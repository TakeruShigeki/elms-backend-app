package com.everrefine.elms.infrastructure.row;

import java.util.UUID;

/** ユーザーごとの完了レッスン数の集約結果。 */
public record CompletedLessonCountRow(UUID userId, Integer completedCount) {}
