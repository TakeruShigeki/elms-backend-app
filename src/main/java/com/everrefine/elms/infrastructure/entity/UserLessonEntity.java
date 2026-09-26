package com.everrefine.elms.infrastructure.entity;

import com.everrefine.elms.domain.model.UserLesson;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** ユーザーレッスンのエンティティ。 */
@Table("user_lessons")
public record UserLessonEntity(
    @Id UUID id, UUID userId, UUID lessonId, LocalDateTime createdAt, LocalDateTime updatedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param userLesson ユーザーレッスンのドメインモデル
   * @return エンティティ
   */
  public static UserLessonEntity from(UserLesson userLesson) {
    return new UserLessonEntity(
        userLesson.id(),
        userLesson.userId(),
        userLesson.lessonId(),
        userLesson.createdAt(),
        userLesson.updatedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return ユーザーレッスンのドメインモデル
   */
  public UserLesson toDomain() {
    return new UserLesson(id, userId, lessonId, createdAt, updatedAt);
  }
}
