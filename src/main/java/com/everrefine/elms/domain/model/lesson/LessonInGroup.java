package com.everrefine.elms.domain.model.lesson;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.lang.Nullable;

/** レッスングループ配下の1レッスンを表す読み取りモデル。 */
public record LessonInGroup(
    UUID id,
    String title,
    BigDecimal lessonOrder,
    @Nullable String content,
    @Nullable String videoUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
