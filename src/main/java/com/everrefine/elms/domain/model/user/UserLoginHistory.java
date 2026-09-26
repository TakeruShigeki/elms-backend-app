package com.everrefine.elms.domain.model.user;

import java.time.LocalDateTime;
import java.util.UUID;

/** ユーザーログイン履歴のドメインモデル。 */
public record UserLoginHistory(
    UUID id, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt) {

  /**
   * 新規作成用のログイン履歴を作成する。
   *
   * @param userId ユーザーID
   * @return 新規作成用のログイン履歴
   */
  public static UserLoginHistory create(UUID userId) {
    LocalDateTime now = LocalDateTime.now();
    return new UserLoginHistory(null, userId, now, now);
  }

  /**
   * ログイン日時を現在時刻に更新する。
   *
   * @return 更新されたログイン履歴
   */
  public UserLoginHistory update() {
    return new UserLoginHistory(id, userId, createdAt, LocalDateTime.now());
  }
}
