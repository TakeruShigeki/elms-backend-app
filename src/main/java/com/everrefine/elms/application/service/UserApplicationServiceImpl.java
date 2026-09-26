package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.LoginHistoryCreateCommand;
import com.everrefine.elms.application.command.PasswordUpdateCommand;
import com.everrefine.elms.application.command.UserCreateCommand;
import com.everrefine.elms.application.command.UserImportCommand;
import com.everrefine.elms.application.command.UserSearchCommand;
import com.everrefine.elms.application.command.UserUpdateCommand;
import com.everrefine.elms.application.dto.AuthenticatedUserDto;
import com.everrefine.elms.application.dto.UserDto;
import com.everrefine.elms.application.dto.UserImportResponseDto;
import com.everrefine.elms.application.dto.UserPageDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.ProgressRate;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserLoginHistory;
import com.everrefine.elms.domain.model.user.UserName;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.domain.repository.UserLessonRepository;
import com.everrefine.elms.domain.repository.UserLoginHistoryRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import com.everrefine.elms.domain.service.UserDomainService;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** ユーザーアプリケーションサービスの実装。 */
@Service
@AllArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

  private final UserRepository userRepository;
  private final UserLoginHistoryRepository userLoginHistoryRepository;
  private final UserLessonRepository userLessonRepository;
  private final LessonRepository lessonRepository;
  private final UserDomainService userDomainService;

  /**
   * CSV出力用に値をエスケープする。
   *
   * <p>値にカンマ、ダブルクォーテーション、改行があった場合のエスケープ処理。
   *
   * @param value エスケープ対象の文字列
   * @return エスケープ後の文字列
   */
  private String escape(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",")
        || value.contains("\"")
        || value.contains("\n")
        || value.contains("\r")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto findUserById(UUID userId) {
    User user =
        userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(User.class, String.valueOf(userId)));
    UserLoginHistory userLoginHistory =
        userLoginHistoryRepository.findUserLoginHistoryByUserId(user.id()).orElse(null);
    int completedLessonCnt = userLessonRepository.countAllByUserId(user.id());
    int allLessonsCnt = lessonRepository.countAllLessons();
    ProgressRate progressRate = ProgressRate.of(completedLessonCnt, allLessonsCnt);
    return UserDto.from(user, userLoginHistory, progressRate.toBigDecimal());
  }

  @Override
  @Transactional(readOnly = true)
  public AuthenticatedUserDto findAuthenticatedUserById(UUID userId) {
    User user =
        userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(User.class, String.valueOf(userId)));
    return AuthenticatedUserDto.from(user);
  }

  @Override
  @Transactional(readOnly = true)
  public AuthenticatedUserDto findAuthenticatedUserByEmail(String emailAddress) {
    User user =
        userRepository
            .findUserByEmailAddress(new EmailAddress(emailAddress))
            .orElseThrow(() -> new ResourceNotFoundException(User.class, emailAddress));
    return AuthenticatedUserDto.from(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserPageDto findUsers(UserSearchCommand userSearchCommand) {
    var userSearchCondition = userSearchCommand.toSearchCondition();
    List<UUID> userIds = userRepository.findUserIdsBySearchConditions(userSearchCondition);
    List<User> users = userRepository.findUsersByIds(userIds);

    if (users.isEmpty()) {
      int totalSize = userRepository.countUsers(userSearchCondition);
      return UserPageDto.from(
          List.of(),
          userSearchCondition.pagerForRequest().pageNum(),
          userSearchCondition.pagerForRequest().pageSize(),
          totalSize);
    }

    // ユーザーIDと、そのユーザーのログイン履歴のMap
    Map<UUID, UserLoginHistory> loginHistoryMap =
        userLoginHistoryRepository.findByUserIdsAsMap(userIds);
    Map<UUID, Integer> completedLessonCounts = userLessonRepository.countByUserIds(userIds);
    int allLessonsCnt = lessonRepository.countAllLessons();

    List<UserDto> userDtos =
        users.stream()
            .map(
                user -> {
                  UserLoginHistory userLoginHistory = loginHistoryMap.get(user.id());
                  int completedLessonCnt = completedLessonCounts.getOrDefault(user.id(), 0);
                  ProgressRate progressRate = ProgressRate.of(completedLessonCnt, allLessonsCnt);
                  return UserDto.from(user, userLoginHistory, progressRate.toBigDecimal());
                })
            .toList();

    int totalSize = userRepository.countUsers(userSearchCondition);
    return UserPageDto.from(
        userDtos,
        userSearchCondition.pagerForRequest().pageNum(),
        userSearchCondition.pagerForRequest().pageSize(),
        totalSize);
  }

  @Override
  @Transactional
  public void createUser(UserCreateCommand userCreateCommand) {
    if (!userDomainService.matchesPassword(
        userCreateCommand.password(), userCreateCommand.confirmPassword())) {
      throw new BadRequestException("パスワードの確認が一致しません");
    }

    validateEmailAddressNotUsedByOtherUser(userCreateCommand.emailAddress(), null);
    validateUserNameNotUsedByOtherUser(userCreateCommand.userName(), null);

    userRepository.createUser(userCreateCommand.toUser());
  }

  @Override
  @Transactional
  public void updateUser(UserUpdateCommand userUpdateCommand) {
    User user =
        userRepository
            .findUserById(userUpdateCommand.id())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        User.class, String.valueOf(userUpdateCommand.id())));

    validateEmailAddressNotUsedByOtherUser(userUpdateCommand.emailAddress(), user.id());
    validateUserNameNotUsedByOtherUser(userUpdateCommand.userName(), user.id());

    userRepository.updateUser(userUpdateCommand.toUser(user));
  }

  /**
   * メールアドレスが他のユーザーに使用されていないことを検証する。
   *
   * @param emailAddress 検証対象のメールアドレス
   * @param excludedUserId 検証から除外するユーザーID（更新時は更新対象のユーザーID、登録時はnull）
   * @throws BadRequestException 他のユーザーがすでに使用している場合
   */
  private void validateEmailAddressNotUsedByOtherUser(String emailAddress, UUID excludedUserId) {
    if (emailAddress == null) {
      return;
    }

    userRepository
        .findUserByEmailAddress(new EmailAddress(emailAddress))
        .filter(existingUser -> !existingUser.id().equals(excludedUserId))
        .ifPresent(
            existingUser -> {
              throw new BadRequestException("このメールアドレスはすでに使用されています");
            });
  }

  /**
   * ユーザー名が他のユーザーに使用されていないことを検証する。
   *
   * @param userName 検証対象のユーザー名
   * @param excludedUserId 検証から除外するユーザーID（更新時は更新対象のユーザーID、登録時はnull）
   * @throws BadRequestException 他のユーザーがすでに使用している場合
   */
  private void validateUserNameNotUsedByOtherUser(String userName, UUID excludedUserId) {
    if (userName == null) {
      return;
    }

    userRepository
        .findUserByUserName(new UserName(userName))
        .filter(existingUser -> !existingUser.id().equals(excludedUserId))
        .ifPresent(
            existingUser -> {
              throw new BadRequestException("このユーザー名はすでに使用されています");
            });
  }

  @Override
  @Transactional
  public void deleteUserById(UUID userId) {
    // ユーザーが存在しなくてもエラーにはしない。
    userRepository.findUserById(userId).ifPresent(user -> userRepository.deleteUserById(userId));
  }

  @Transactional
  @Override
  public void updateUserLoginHistory(LoginHistoryCreateCommand loginHistoryCreateCommand) {
    // 認証成功後に呼ばれるため、通常ここでユーザーが見つからないことはない。
    // Spring Securityの UsernameNotFoundException を投げると AuthenticationException として
    // コントローラーに握りつぶされ、データ不整合が「認証失敗」の401に化けてしまうため使用しない。
    User user =
        userRepository
            .findUserByEmailAddress(new EmailAddress(loginHistoryCreateCommand.email()))
            .orElseThrow(
                () -> new ResourceNotFoundException(User.class, loginHistoryCreateCommand.email()));

    Optional<UserLoginHistory> userLoginHistory =
        userLoginHistoryRepository.findUserLoginHistoryByUserId(user.id());

    // 該当ユーザーのログイン履歴がすでにあれば更新、なければ作成
    userLoginHistory.ifPresentOrElse(
        history -> {
          userLoginHistoryRepository.save(history.update());
        },
        () -> {
          userLoginHistoryRepository.save(UserLoginHistory.create(user.id()));
        });
  }

  @Transactional(readOnly = true)
  @Override
  public Resource exportUsersCsv() {

    String[] header = {"ユーザーID", "権限", "氏名", "メールアドレス", "ユーザー名", "作成日時", "最終ログイン日時", "進捗率"};

    List<User> users = userRepository.findAllByOrderByCreatedAtAscIdAsc();

    if (users.isEmpty()) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (PrintWriter file =
          new PrintWriter(
              new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8)))) {
        file.print('\uFEFF');
        file.println(String.join(",", header));
      } catch (Exception e) {
        throw new RuntimeException("CSVファイルの作成に失敗しました", e);
      }
      return new ByteArrayResource(baos.toByteArray());
    }

    // ユーザーIDと、そのユーザーのログイン履歴のMap
    List<UUID> userIds = users.stream().map(User::id).toList();
    Map<UUID, UserLoginHistory> loginHistoryMap =
        userLoginHistoryRepository.findByUserIdsAsMap(userIds);
    Map<UUID, Integer> completedLessonCounts = userLessonRepository.countByUserIds(userIds);
    int allLessonsCnt = lessonRepository.countAllLessons();

    // 最終ログイン日時と進捗率をusersに追加する
    List<UserDto> userDtos =
        users.stream()
            .map(
                user -> {
                  UserLoginHistory userLoginHistory = loginHistoryMap.get(user.id());
                  int completedLessonCnt = completedLessonCounts.getOrDefault(user.id(), 0);
                  ProgressRate progressRate = ProgressRate.of(completedLessonCnt, allLessonsCnt);
                  return UserDto.from(user, userLoginHistory, progressRate.toBigDecimal());
                })
            .toList();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (
    // 書き込むファイルを作成
    PrintWriter file =
        new PrintWriter(
            new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))); ) {
      // Excelで開いたときの文字化けを防ぐ
      file.print('\uFEFF');

      // ヘッダーをセットする
      file.println(String.join(",", header));

      // 中身をセットする ( 1ユーザーごとに改行 )
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
      for (UserDto userDto : userDtos) {
        String[] user = {
          escape(String.valueOf(userDto.id())),
          escape(userDto.userRole()),
          escape(userDto.realName()),
          escape(userDto.emailAddress()),
          escape(userDto.userName()),
          escape(userDto.createdAt().format(formatter)),
          userDto.lastLoginAt() == null ? "" : escape(userDto.lastLoginAt().format(formatter)),
          escape(userDto.progressRate().toString() + "%")
        };
        file.println(String.join(",", user));
      }
    } catch (Exception e) {
      throw new RuntimeException("CSVファイルの作成に失敗しました", e);
    }
    return new ByteArrayResource(baos.toByteArray());
  }

  @Transactional
  @Override
  public void updatePassword(PasswordUpdateCommand passwordUpdateCommand) {
    User user = findLoginUser();

    if (!user.isCurrentPasswordMatch(passwordUpdateCommand.currentPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    userRepository.updateUser(
        passwordUpdateCommand.toUser(user, passwordUpdateCommand.newPassword()));
  }

  /**
   * 現在ログイン中のユーザーを取得する。
   *
   * @return 現在ログイン中のユーザー
   */
  private User findLoginUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    UUID userId = UUID.fromString(userDetails.getUsername());
    return userRepository
        .findUserById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(User.class, String.valueOf(userId)));
  }

  /**
   * CSVファイルをアップロードしてユーザー情報を一括更新する。CSVの内容でユーザーを洗い替え（DELETE → INSERT）する。
   *
   * @param userImportCommand ユーザー取込用Command
   * @return 取込した件数
   */
  @Override
  @Transactional
  public UserImportResponseDto importUsersCsv(UserImportCommand userImportCommand) {
    User currentUser = findCurrentUser(userImportCommand);
    throwExceptionIfCurrentUserNotIncluded(userImportCommand, currentUser);
    throwExceptionIfAdminNotIncluded(userImportCommand);
    List<User> users = userImportCommand.toUsersKeepingCurrentUserId(currentUser);

    userRepository.deleteAllUsers();
    userRepository.saveAllUsers(users);

    return new UserImportResponseDto(userImportCommand.getImportCount());
  }

  /**
   * 現在ログイン中のユーザーを取得する。
   *
   * @param userImportCommand ユーザー取込用Command
   * @return 現在ログイン中のユーザー
   */
  private User findCurrentUser(UserImportCommand userImportCommand) {
    return userRepository
        .findUserById(userImportCommand.currentUserId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    User.class, String.valueOf(userImportCommand.currentUserId())));
  }

  /**
   * CSV内に現在ログイン中のユーザーが含まれていない場合に例外をスローする。
   *
   * @param userImportCommand ユーザー取込用Command
   * @param currentUser 現在ログイン中のユーザー
   */
  private void throwExceptionIfCurrentUserNotIncluded(
      UserImportCommand userImportCommand, User currentUser) {
    boolean currentUserExists =
        userImportCommand.rows().stream()
            .anyMatch(row -> row.hasEmailAddress(currentUser.emailAddress()));
    if (!currentUserExists) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "現在ログイン中のユーザーがCSVに含まれていません");
    }
  }

  /**
   * CSV内に管理者ユーザーが1人も含まれていない場合に例外をスローする。
   *
   * @param userImportCommand ユーザー取込用Command
   */
  private void throwExceptionIfAdminNotIncluded(UserImportCommand userImportCommand) {
    if (!userImportCommand.containsAdmin()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "管理者ユーザーを1人以上含めてください");
    }
  }
}
