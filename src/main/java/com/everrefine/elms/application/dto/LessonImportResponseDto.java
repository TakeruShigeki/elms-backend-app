package com.everrefine.elms.application.dto;

/** レッスンCSV取込レスポンス用DTO。取込したレッスングループ件数とレッスン件数を返却する。 */
public record LessonImportResponseDto(int importedLessonGroupCount, int importedLessonCount) {}
