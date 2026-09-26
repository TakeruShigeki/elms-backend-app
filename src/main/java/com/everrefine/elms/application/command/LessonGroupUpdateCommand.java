package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.lesson.LessonGroup;
import java.util.UUID;

/** レッスングループ更新用のコマンド。 */
public record LessonGroupUpdateCommand(UUID id, String title) {

  /**
   * LessonGroupエンティティに変換する。
   *
   * @param lessonGroup 更新対象のレッスングループ
   * @return 更新後のレッスングループエンティティ
   */
  public LessonGroup toLessonGroup(LessonGroup lessonGroup) {
    return lessonGroup.update(title);
  }
}
