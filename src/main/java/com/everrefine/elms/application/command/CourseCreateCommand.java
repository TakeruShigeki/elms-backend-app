package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.course.Course;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.lang.Nullable;

/** 新規作成用コースのコマンド。 */
public record CourseCreateCommand(
    @Nullable UUID id,
    @Nullable String thumbnailUrl,
    @NotNull String title,
    @Nullable String description) {

  /**
   * Courseエンティティに変換する。
   *
   * @param courseOrder コース順序
   * @return コースエンティティ
   */
  public Course toCourse(BigDecimal courseOrder) {
    return Course.create(thumbnailUrl, title, description, courseOrder);
  }
}
