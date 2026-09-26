package com.everrefine.elms.domain.model.user;

import com.everrefine.elms.domain.model.ThumbnailUrl;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.lang.Nullable;

/** ユーザーのドメインモデル。 */
public record User(
    UUID id,
    EmailAddress emailAddress,
    Password password,
    RealName realName,
    UserName userName,
    @Nullable ThumbnailUrl thumbnailUrl,
    UserRole userRole,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * 新規作成用のユーザーを作成する。
   *
   * @param emailAddress メールアドレス
   * @param password パスワード
   * @param realName 本名
   * @param userName ユーザー名
   * @param thumbnailUrl サムネイル画像のURL
   * @param userRole 権限
   * @return 新規作成用のユーザー
   */
  public static User create(
      String emailAddress,
      String password,
      String realName,
      String userName,
      String thumbnailUrl,
      UserRole userRole) {
    LocalDateTime now = LocalDateTime.now();
    return new User(
        null,
        new EmailAddress(emailAddress),
        Password.encryptAndCreate(password),
        new RealName(realName),
        new UserName(userName),
        thumbnailUrl == null ? null : new ThumbnailUrl(thumbnailUrl),
        userRole,
        now,
        now);
  }

  /**
   * 更新用のユーザーを作成する。
   *
   * @param emailAddress メールアドレス
   * @param password パスワード
   * @param realName 本名
   * @param userName ユーザー名
   * @param thumbnailUrl サムネイル画像のURL
   * @param userRole 権限
   * @return 更新用のユーザー
   */
  public User update(
      String emailAddress,
      String password,
      String realName,
      String userName,
      String thumbnailUrl,
      UserRole userRole) {
    return new User(
        this.id,
        emailAddress == null ? this.emailAddress : new EmailAddress(emailAddress),
        password == null ? this.password : Password.encryptAndCreate(password),
        realName == null ? this.realName : new RealName(realName),
        userName == null ? this.userName : new UserName(userName),
        thumbnailUrl == null ? this.thumbnailUrl : new ThumbnailUrl(thumbnailUrl),
        userRole == null ? this.userRole : userRole,
        this.createdAt,
        LocalDateTime.now());
  }

  /**
   * 該当のパスワードと自身のパスワードが一致するかを判定します。
   *
   * @param currentPassword 該当のパスワード
   * @return 一致する場合はtrue,不一致の場合はfalse
   */
  public boolean isCurrentPasswordMatch(String currentPassword) {
    return this.password.matches(currentPassword);
  }
}
