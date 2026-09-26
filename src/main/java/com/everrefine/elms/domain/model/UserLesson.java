package com.everrefine.elms.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/** ユーザーレッスンのドメインモデル。 */
public record UserLesson(
    UUID id, UUID userId, UUID lessonId, LocalDateTime createdAt, LocalDateTime updatedAt) {

  /**
   * 新規作成用のユーザーレッスンを作成する。
   *
   * @param userId ユーザーID
   * @param lessonId レッスンID
   * @return 新規作成用のユーザーレッスン
   */
  public static UserLesson create(UUID userId, UUID lessonId) {
    LocalDateTime now = LocalDateTime.now();
    return new UserLesson(null, userId, lessonId, now, now);
  }

  /**
   * updated_atを現在時刻に更新する。
   *
   * @return 更新されたユーザーレッスン
   */
  public UserLesson update() {
    return new UserLesson(this.id, this.userId, this.lessonId, this.createdAt, LocalDateTime.now());
  }
}
