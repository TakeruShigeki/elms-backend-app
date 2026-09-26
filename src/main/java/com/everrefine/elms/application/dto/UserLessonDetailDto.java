package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.Lesson;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** ユーザーレッスン詳細。 */
public record UserLessonDetailDto(
    @Schema(description = "レッスンID", example = "1") UUID id,
    @Schema(description = "レッスングループID", example = "2") UUID lessonGroupId,
    @Schema(description = "コースID", example = "3") UUID courseId,
    @Schema(description = "レッスンの表示順", example = "1.0") BigDecimal lessonOrder,
    @Schema(description = "レッスンタイトル", example = "変数とデータ型") String title,
    @Schema(description = "レッスン本文（Markdown対応）", example = "## 変数とは\n変数はデータを格納する箱です。")
        String content,
    @Schema(description = "動画URL", example = "https://example.com/videos/lesson1.mp4")
        String videoUrl,
    @Schema(description = "登録日時", example = "2024-01-01T09:00:00") LocalDateTime createdAt,
    @Schema(description = "更新日時", example = "2024-06-01T10:30:00") LocalDateTime updatedAt,
    @Schema(description = "レッスン完了フラグ（true: 完了, false: 未完了）", example = "false")
        @JsonProperty("isLessonCompleted")
        boolean lessonCompleted) {

  /**
   * LessonエンティティからUserLessonDetailDtoを生成する。
   *
   * @param lesson レッスンエンティティ
   * @param isLessonCompleted レッスン完了フラグ
   * @return ユーザーレッスン詳細DTO
   */
  public static UserLessonDetailDto from(Lesson lesson, boolean isLessonCompleted) {
    return new UserLessonDetailDto(
        lesson.id(),
        lesson.lessonGroupId(),
        lesson.courseId(),
        lesson.lessonOrder().value(),
        lesson.title().value(),
        lesson.content() != null ? lesson.content().value() : null,
        lesson.videoUrl() != null ? lesson.videoUrl().value() : null,
        lesson.createdAt(),
        lesson.updatedAt(),
        isLessonCompleted);
  }
}
