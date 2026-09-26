package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.course.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** ユーザーコースDTO。 */
public record UserCourseDto(
    @Schema(description = "コースID", example = "1") UUID id,
    @Schema(description = "コースの表示順", example = "1.0") BigDecimal courseOrder,
    @Schema(description = "サムネイルURL", example = "https://example.com/course-thumbnail.png")
        String thumbnailUrl,
    @Schema(description = "コースタイトル", example = "Javaプログラミング入門") String title,
    @Schema(description = "コース説明", example = "Javaの基礎から応用まで学べるコースです") String description,
    @Schema(description = "登録日時", example = "2024-01-01T09:00:00") LocalDateTime createdAt,
    @Schema(description = "更新日時", example = "2024-06-01T10:30:00") LocalDateTime updatedAt,
    @Schema(description = "進捗", example = "65.0") BigDecimal courseProgress) {

  /**
   * CourseエンティティからUserCourseDtoを生成する。
   *
   * @param course コースエンティティ
   * @param courseProgress コース進捗率
   * @return ユーザーコースDTO
   */
  public static UserCourseDto from(Course course, BigDecimal courseProgress) {
    return new UserCourseDto(
        course.id(),
        course.courseOrder().value(),
        course.thumbnailUrl() != null ? course.thumbnailUrl().value() : null,
        course.title().value(),
        course.description() != null ? course.description().value() : null,
        course.createdAt(),
        course.updatedAt(),
        courseProgress);
  }
}
