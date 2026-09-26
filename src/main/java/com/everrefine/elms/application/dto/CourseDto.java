package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.course.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** コースDTO。 */
public record CourseDto(
    @Schema(description = "コースID", example = "1") UUID id,
    @Schema(description = "コースの表示順", example = "1.0") BigDecimal courseOrder,
    @Schema(description = "サムネイルURL", example = "https://example.com/course-thumbnail.png")
        String thumbnailUrl,
    @Schema(description = "コースタイトル", example = "Javaプログラミング入門") String title,
    @Schema(description = "コース説明", example = "Javaの基礎から応用まで学べるコースです") String description,
    @Schema(description = "登録日時", example = "2024-01-01T09:00:00") LocalDateTime createdAt,
    @Schema(description = "更新日時", example = "2024-06-01T10:30:00") LocalDateTime updatedAt) {

  /**
   * CourseエンティティからCourseDtoを生成する。
   *
   * @param course コースエンティティ
   * @return コースDTO
   */
  public static CourseDto from(Course course) {
    return new CourseDto(
        course.id(),
        course.courseOrder().value(),
        course.thumbnailUrl() != null ? course.thumbnailUrl().value() : null,
        course.title().value(),
        course.description() != null ? course.description().value() : null,
        course.createdAt(),
        course.updatedAt());
  }
}
