package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.lesson.Lesson;
import java.math.BigDecimal;
import java.util.UUID;

/** レッスン作成用のコマンド。 */
public record LessonCreateCommand(
    UUID courseId,
    UUID lessonGroupId,
    String title,
    String content,
    String videoUrl,
    BigDecimal lessonOrder) {

  /**
   * Lessonエンティティに変換する。
   *
   * @param lessonOrder レッスン順序
   * @return レッスンエンティティ
   */
  public Lesson toLesson(BigDecimal lessonOrder) {
    return Lesson.create(lessonGroupId, courseId, lessonOrder, title, content, videoUrl);
  }
}
