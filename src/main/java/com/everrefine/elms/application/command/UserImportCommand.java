package com.everrefine.elms.application.command;

import com.everrefine.elms.application.util.CsvImportUtils;
import com.everrefine.elms.domain.exception.InvalidValueException;
import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.RealName;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserName;
import com.everrefine.elms.domain.model.user.UserRole;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** CSV取込用ユーザーのコマンド。CSV全体のユーザー情報を保持する。 */
public record UserImportCommand(UUID currentUserId, List<UserImportRowCommand> rows) {

  private static final String[] EXPECTED_HEADER = {"権限", "氏名", "メールアドレス", "ユーザー名"};

  /**
   * CSVファイルを読み込み、取込用Commandに変換する。
   *
   * @param file アップロード対象のCSVファイル
   * @param currentUserId 現在ログイン中のユーザーID
   * @return 取込用Command
   */
  public static UserImportCommand from(MultipartFile file, UUID currentUserId) {
    throwExceptionIfCsvFileInvalid(file);
    List<UserImportRowCommand> rows = readRows(file);
    throwExceptionIfRowsDuplicated(rows);
    return new UserImportCommand(currentUserId, rows);
  }

  /**
   * 取込対象のユーザー件数を取得する。
   *
   * @return 取込件数
   */
  public int getImportCount() {
    return rows.size();
  }

  /**
   * 現在ログイン中ユーザーのみ既存IDを維持して、保存用ユーザーリストを作成する。
   *
   * <p>新規ユーザーのIDはアプリケーション側で採番する。一括登録では {@code save()} を経由せず直接INSERTするため、
   * DB採番に頼らずともIDを確定でき、登録直後からIDを利用できる。
   *
   * @param currentUser 現在ログイン中ユーザー
   * @return 保存用ユーザーリスト
   */
  public List<User> toUsersKeepingCurrentUserId(User currentUser) {
    return rows.stream()
        .map(
            row ->
                row.toUser(
                    row.hasEmailAddress(currentUser.emailAddress())
                        ? currentUser.id()
                        : UUID.randomUUID()))
        .toList();
  }

  /**
   * CSV内に管理者ユーザーが含まれているか判定する。
   *
   * @return 管理者ユーザーが含まれている場合はtrue
   */
  public boolean containsAdmin() {
    return rows.stream().anyMatch(row -> row.userRole() == UserRole.ADMIN);
  }

  /**
   * CSVファイルが不正な場合に例外をスローする。
   *
   * @param file アップロード対象のCSVファイル
   */
  private static void throwExceptionIfCsvFileInvalid(MultipartFile file) {
    CsvImportUtils.throwExceptionIfCsvFileInvalid(
        file == null ? null : file.getOriginalFilename(),
        file == null || file.isEmpty(),
        UserImportCommand::badRequest);
  }

  /**
   * CSVファイルを読み込み、行Commandのリストに変換する。
   *
   * @param file アップロード対象のCSVファイル
   * @return 行Commandのリスト
   */
  private static List<UserImportRowCommand> readRows(MultipartFile file) {
    try {
      return CsvImportUtils.readRows(
          file.getBytes(),
          EXPECTED_HEADER,
          UserImportCommand::toUserImportRowCommand,
          UserImportCommand::badRequest,
          ResponseStatusException.class,
          false);
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      throw badRequest("CSVファイルの解析に失敗しました: " + e.getMessage());
    }
  }

  /**
   * CSVの行データを行Commandに変換する。
   *
   * @param values CSVの行データ
   * @param lineNumber CSV上の行番号
   * @return 行Command
   */
  private static UserImportRowCommand toUserImportRowCommand(String[] values, int lineNumber) {
    String roleStr = values[0].trim();
    String realName = values[1].trim();
    String emailAddress = values[2].trim();
    String userName = values[3].trim();

    if (roleStr.isEmpty() || realName.isEmpty() || emailAddress.isEmpty() || userName.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "行" + lineNumber + ": 必須項目が入力されていません");
    }

    UserRole userRole;
    try {
      userRole = UserRole.fromRoleName(roleStr);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "行" + lineNumber + ": 権限が不正です");
    }

    throwExceptionIfValueInvalid(realName, emailAddress, userName, lineNumber);

    return new UserImportRowCommand(userRole, realName, emailAddress, userName);
  }

  /**
   * 氏名・メールアドレス・ユーザー名が、ユーザー作成時と同じ制約を満たさない場合に例外をスローする。
   *
   * <p>値オブジェクトの生成をここで先に試すことで、制約の定義をドメイン側の一箇所に保ちつつ、 CSVの不備を取込前に行番号付きの {@code 400 Bad Request}
   * として返せるようにする。 この検証がないと、登録処理の途中で値オブジェクトの生成に失敗し、DBのデータ不整合と同じ扱いで {@code 500} を返してしまう。
   *
   * @param realName 氏名
   * @param emailAddress メールアドレス
   * @param userName ユーザー名
   * @param lineNumber CSV上の行番号
   */
  private static void throwExceptionIfValueInvalid(
      String realName, String emailAddress, String userName, int lineNumber) {
    try {
      new RealName(realName);
      new EmailAddress(emailAddress);
      new UserName(userName);
    } catch (InvalidValueException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "行" + lineNumber + ": " + e.getMessage());
    }
  }

  /**
   * CSV内にメールアドレスまたはユーザー名の重複がある場合に例外をスローする。
   *
   * @param rows 行Commandのリスト
   */
  private static void throwExceptionIfRowsDuplicated(List<UserImportRowCommand> rows) {
    Set<String> emailAddresses = new HashSet<>();
    Set<String> userNames = new HashSet<>();

    for (UserImportRowCommand row : rows) {
      if (!emailAddresses.add(row.emailAddress())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "CSV内に重複するメールアドレスが存在します: " + row.emailAddress());
      }
      if (!userNames.add(row.userName())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "CSV内に重複するユーザー名が存在します: " + row.userName());
      }
    }
  }

  private static ResponseStatusException badRequest(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }
}
