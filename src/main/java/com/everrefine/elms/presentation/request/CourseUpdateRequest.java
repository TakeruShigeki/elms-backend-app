package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.CourseUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** コース更新リクエスト。 */
public record CourseUpdateRequest(
    @Schema(description = "コースID", example = "1") UUID courseId,
    @Schema(description = "コースの表示順（整数部6桁・小数部4桁以内）", example = "1.0")
        @NotNull(message = "コースの表示順は必須です") @Positive(message = "コースの表示順は1以上で指定してください") @Digits(integer = 6, fraction = 4, message = "コースの表示順は整数部6桁・小数部4桁以内で指定してください")
        BigDecimal courseOrder,
    @Schema(description = "コースタイトル（必須・255文字以内）", example = "Javaプログラミング入門")
        @NotBlank(message = "コースタイトルは必須です")
        @Size(max = 255, message = "コースタイトルは255文字以内で入力してください")
        String title,
    @Schema(description = "コース説明（1000000文字以内）", example = "Javaの基礎から応用まで学べるコースです")
        @Size(max = 1_000_000, message = "コース説明は1000000文字以内で入力してください")
        String description,
    @Schema(
            description = "サムネイルURL（2048文字以内）",
            example = "https://example.com/course-thumbnail.png")
        @Size(max = 2048, message = "サムネイルURLは2048文字以内で入力してください")
        String thumbnailUrl) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @param courseId コースID
   * @return コース更新Command
   */
  public CourseUpdateCommand toCommand(UUID courseId) {
    return new CourseUpdateCommand(
        courseId, courseOrder, title, description, thumbnailUrl, LocalDateTime.now());
  }
}
