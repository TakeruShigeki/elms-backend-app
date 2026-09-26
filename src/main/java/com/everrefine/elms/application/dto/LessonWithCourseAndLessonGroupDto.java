package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.LessonWithCourseAndLessonGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** 全レッスンCSV出力用 DTO */
public record LessonWithCourseAndLessonGroupDto(
    @Schema(description = "コースID", example = "1") UUID courseId,
    @Schema(description = "レッスングループID", example = "2") UUID lessonGroupId,
    @Schema(description = "レッスンID", example = "3") UUID lessonId,
    @Schema(description = "コースタイトル", example = "Javaコース") String courseTitle,
    @Schema(description = "レッスングループタイトル", example = "Java導入") String lessonGroupTitle,
    @Schema(description = "レッスンタイトル", example = "変数とデータ型") String lessonTitle,
    @Schema(description = "動画URL", example = "https://example.com/videos/lesson1.mp4")
        String videoUrl) {

  /**
   * LessonWithCourseAndLessonGroupからDTOを生成する。
   *
   * @param source コース・レッスングループ・レッスンの結合情報
   * @return レッスン詳細DTO
   */
  public static LessonWithCourseAndLessonGroupDto from(LessonWithCourseAndLessonGroup source) {
    return new LessonWithCourseAndLessonGroupDto(
        source.courseId(),
        source.lessonGroupId(),
        source.lessonId(),
        source.courseTitle(),
        source.lessonGroupTitle(),
        source.lessonTitle(),
        source.lessonVideoUrl());
  }
}
