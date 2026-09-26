package com.everrefine.elms.infrastructure.row;

import com.everrefine.elms.domain.model.lesson.LessonWithCourseAndLessonGroup;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;

/** コース・レッスングループ・レッスンをJOINしたセレクト結果。 */
public record LessonWithCourseAndLessonGroupRow(
    @Id UUID courseId,
    String courseTitle,
    UUID lessonGroupId,
    String lessonGroupTitle,
    UUID lessonId,
    String lessonTitle,
    @Nullable String lessonVideoUrl) {

  /**
   * ドメインの読み取りモデルに変換する。
   *
   * @return ドメインの読み取りモデル
   */
  public LessonWithCourseAndLessonGroup toDomain() {
    return new LessonWithCourseAndLessonGroup(
        courseId,
        courseTitle,
        lessonGroupId,
        lessonGroupTitle,
        lessonId,
        lessonTitle,
        lessonVideoUrl);
  }
}
