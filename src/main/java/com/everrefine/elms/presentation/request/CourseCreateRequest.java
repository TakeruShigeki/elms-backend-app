package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.CourseCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** コース作成リクエスト。 */
public record CourseCreateRequest(
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
   * @return コース作成Command
   */
  public CourseCreateCommand toCommand() {
    return new CourseCreateCommand(null, thumbnailUrl, title, description);
  }
}
