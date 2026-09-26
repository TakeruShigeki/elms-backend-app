package com.everrefine.elms.infrastructure.entity.course;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.ThumbnailUrl;
import com.everrefine.elms.domain.model.course.Course;
import com.everrefine.elms.domain.model.course.CourseDescription;
import com.everrefine.elms.domain.model.course.CourseTitle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

/** コースのエンティティ。 */
@Table("courses")
public record CourseEntity(
    @Id UUID id,
    @Nullable String thumbnailUrl,
    String title,
    @Nullable String description,
    BigDecimal courseOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param course コースのドメインモデル
   * @return エンティティ
   */
  public static CourseEntity from(Course course) {
    return new CourseEntity(
        course.id(),
        course.thumbnailUrl() != null ? course.thumbnailUrl().value() : null,
        course.title().value(),
        course.description() != null ? course.description().value() : null,
        course.courseOrder().value(),
        course.createdAt(),
        course.updatedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return コースのドメインモデル
   */
  public Course toDomain() {
    return new Course(
        id,
        thumbnailUrl != null ? new ThumbnailUrl(thumbnailUrl) : null,
        new CourseTitle(title),
        description != null ? new CourseDescription(description) : null,
        new Order(courseOrder),
        createdAt,
        updatedAt);
  }
}
