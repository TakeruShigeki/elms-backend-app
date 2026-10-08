package com.everrefine.elms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** タグのリクエスト。 */
public record TagRequest(
    @Schema(description = "タグ名（必須・50文字以内）", example = "Java")
        @NotBlank(message = "タグ名は必須です")
        @Size(max = 50, message = "タグ名は50文字以内で入力してください")
        String name) {}
