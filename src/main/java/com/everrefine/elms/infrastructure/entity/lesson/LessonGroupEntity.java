package com.everrefine.elms.infrastructure.entity.lesson;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.lesson.LessonGroup;
import com.everrefine.elms.domain.model.lesson.LessonTitle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** レッスングループのエンティティ。 */
@Table("lesson_groups")
public record LessonGroupEntity(
    @Id UUID id,
    UUID courseId,
    BigDecimal lessonGroupOrder,
    String title,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param lessonGroup レッスングループのドメインモデル
   * @return エンティティ
   */
  public static LessonGroupEntity from(LessonGroup lessonGroup) {
    return new LessonGroupEntity(
        lessonGroup.id(),
        lessonGroup.courseId(),
        lessonGroup.lessonGroupOrder().value(),
        lessonGroup.title().value(),
        lessonGroup.createdAt(),
        lessonGroup.updatedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return レッスングループのドメインモデル
   */
  public LessonGroup toDomain() {
    return new LessonGroup(
        id, courseId, new Order(lessonGroupOrder), new LessonTitle(title), createdAt, updatedAt);
  }
}
