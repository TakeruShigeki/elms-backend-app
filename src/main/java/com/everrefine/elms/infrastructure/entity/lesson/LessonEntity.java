package com.everrefine.elms.infrastructure.entity.lesson;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonContent;
import com.everrefine.elms.domain.model.lesson.LessonTitle;
import com.everrefine.elms.domain.model.lesson.VideoUrl;
import com.everrefine.elms.domain.model.tag.Tag;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
   * <p>タグは {@code lessons} テーブルではなく {@code lesson_tags} と {@code tags} が持つため、この行からは復元できない。
   * タグなしのレッスンとして変換する。タグを含めるには {@link #toDomain(List)} を使う。
   *
   * @return レッスンのドメインモデル（タグなし）
   */
  public Lesson toDomain() {
    return toDomain(List.of());
  }

  /**
   * タグを指定してドメインモデルに変換する。
   *
   * @param tags タグのリスト
   * @return レッスンのドメインモデル
   */
  public Lesson toDomain(List<Tag> tags) {
    return new Lesson(
        id,
        lessonGroupId,
        courseId,
        new Order(lessonOrder),
        new LessonTitle(title),
        content != null ? new LessonContent(content) : null,
        videoUrl != null ? new VideoUrl(videoUrl) : null,
        tags,
        createdAt,
        updatedAt);
  }
}
