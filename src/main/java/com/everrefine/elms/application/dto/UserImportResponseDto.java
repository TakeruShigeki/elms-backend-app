package com.everrefine.elms.application.dto;

/** CSV取込レスポンス用DTO。 取込した件数を返却する。 */
public record UserImportResponseDto(int importedCount) {}
