package com.everrefine.elms.domain.model.lesson;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** レッスングループと、その配下のレッスン一覧を表す読み取りモデル。 */
public record LessonGroupWithLessons(
    UUID id,
    UUID courseId,
    String title,
    BigDecimal lessonGroupOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<LessonInGroup> lessons) {}
