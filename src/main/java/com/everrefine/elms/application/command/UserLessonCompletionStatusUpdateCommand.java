package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.UserLesson;
import java.util.UUID;

/** ユーザーレッスン完了状態更新用のコマンド。 */
public record UserLessonCompletionStatusUpdateCommand(
    UUID userId, UUID lessonId, boolean isLessonCompleted) {

  /**
   * 新規UserLessonエンティティに変換する。
   *
   * @return 新規ユーザーレッスンエンティティ
   */
  public UserLesson toNewUserLesson() {
    return UserLesson.create(this.userId, this.lessonId);
  }
}
