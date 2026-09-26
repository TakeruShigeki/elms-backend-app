package com.everrefine.elms.domain.model.lesson;

import java.util.UUID;
import org.springframework.lang.Nullable;

/** コース・レッスングループ・レッスンをJOINした読み取りモデル。 */
public record LessonWithCourseAndLessonGroup(
    UUID courseId,
    String courseTitle,
    UUID lessonGroupId,
    String lessonGroupTitle,
    UUID lessonId,
    String lessonTitle,
    @Nullable String lessonVideoUrl) {}
