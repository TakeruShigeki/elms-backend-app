package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.lesson.Lesson;
import java.util.UUID;

/** レッスン更新用のコマンド。 */
public record LessonUpdateCommand(UUID id, String title, String content, String videoUrl) {

  /**
   * Lessonエンティティに変換する。
   *
   * @param lesson 更新対象のレッスン
   * @return 更新後のレッスンエンティティ
   */
  public Lesson toLesson(Lesson lesson) {
    return lesson.update(title, content, videoUrl);
  }
}
