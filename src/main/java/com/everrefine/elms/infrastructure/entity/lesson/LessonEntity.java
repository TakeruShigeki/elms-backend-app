package com.everrefine.elms.infrastructure.entity.lesson;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonContent;
import com.everrefine.elms.domain.model.lesson.LessonTitle;
import com.everrefine.elms.domain.model.lesson.VideoUrl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

/** レッスンのエンティティ。 */
@Table("lessons")
public record LessonEntity(
    @Id UUID id,
    UUID lessonGroupId,
    UUID courseId,
    BigDecimal lessonOrder,
    String title,
    @Nullable String content,
    @Nullable String videoUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param lesson レッスンのドメインモデル
   * @return エンティティ
   */
  public static LessonEntity from(Lesson lesson) {
    return new LessonEntity(
        lesson.id(),
        lesson.lessonGroupId(),
        lesson.courseId(),
        lesson.lessonOrder().value(),
        lesson.title().value(),
        lesson.content() != null ? lesson.content().value() : null,
        lesson.videoUrl() != null ? lesson.videoUrl().value() : null,
        lesson.createdAt(),
        lesson.updatedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return レッスンのドメインモデル
   */
  public Lesson toDomain() {
    return new Lesson(
        id,
        lessonGroupId,
        courseId,
        new Order(lessonOrder),
        new LessonTitle(title),
        content != null ? new LessonContent(content) : null,
        videoUrl != null ? new VideoUrl(videoUrl) : null,
        createdAt,
        updatedAt);
  }
}
