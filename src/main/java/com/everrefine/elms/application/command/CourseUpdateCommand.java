package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.course.Course;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** 更新用コースのコマンド。 */
public record CourseUpdateCommand(
    @NotNull UUID id,
    @NotNull BigDecimal courseOrder,
    @NotNull String title,
    @Nullable String description,
    @Nullable String thumbnailUrl,
    @NotNull LocalDateTime updatedAt) {

  /**
   * Courseエンティティに変換する。
   *
   * @param course 更新対象のコース
   * @return 更新後のコースエンティティ
   */
  public Course toCourse(Course course) {
    return course.update(thumbnailUrl, title, description, courseOrder);
  }
}
