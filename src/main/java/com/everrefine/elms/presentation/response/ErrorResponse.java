package com.everrefine.elms.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** エラーレスポンス。 */
@Schema(description = "エラーレスポンス")
public record ErrorResponse(
    @Schema(description = "エラーコード", example = "RESOURCE_NOT_FOUND") String code,
    @Schema(description = "エラーメッセージ", example = "リソースが見つかりません") String message) {}
