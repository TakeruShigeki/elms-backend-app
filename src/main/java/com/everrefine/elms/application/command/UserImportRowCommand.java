package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.Password;
import com.everrefine.elms.domain.model.user.RealName;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserName;
import com.everrefine.elms.domain.model.user.UserRole;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/** CSV取込用ユーザー行のコマンド。CSVの1行分のユーザー情報を保持する。 */
public record UserImportRowCommand(
    UserRole userRole, String realName, String emailAddress, String userName) {

  private static final int RANDOM_PASSWORD_LENGTH = 32;
  private static final char[] PASSWORD_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * 保存用ユーザーを作成する。
   *
   * <p>一括登録では {@code save()} を経由せず直接INSERTするため、IDは呼び出し側で採番して渡すこと。
   *
   * @param id ユーザーID
   * @return 保存用ユーザー
   */
  public User toUser(UUID id) {
    LocalDateTime now = LocalDateTime.now();
    return new User(
        id,
        new EmailAddress(emailAddress),
        Password.encryptAndCreate(generateRandomPassword()),
        new RealName(realName),
        new UserName(userName),
        null,
        userRole,
        now,
        now);
  }

  /**
   * 指定されたメールアドレスと一致するか判定する。
   *
   * @param emailAddress 比較対象のメールアドレス
   * @return 一致する場合はtrue
   */
  public boolean hasEmailAddress(EmailAddress emailAddress) {
    return this.emailAddress.equals(emailAddress.value());
  }

  /**
   * ランダムなパスワードを生成する。
   *
   * @return ランダムパスワード文字列
   */
  private static String generateRandomPassword() {
    StringBuilder password = new StringBuilder(RANDOM_PASSWORD_LENGTH);
    for (int i = 0; i < RANDOM_PASSWORD_LENGTH; i++) {
      password.append(PASSWORD_CHARS[SECURE_RANDOM.nextInt(PASSWORD_CHARS.length)]);
    }
    return password.toString();
  }
}
