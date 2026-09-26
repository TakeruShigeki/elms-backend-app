package com.everrefine.elms.application.dto;

/** ユーザーレッスンDTO。 */
public record UserLessonDto(LessonDto lesson, boolean isLessonCompleted) {}
