package com.everrefine.elms.application.command;

import java.util.UUID;

/** レッスン順序更新用のコマンド。 */
public record LessonOrderUpdateCommand(
    UUID lessonId, UUID precedingLessonId, UUID followingLessonId) {}
