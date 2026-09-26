package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.LessonGroupCreateCommand;
import com.everrefine.elms.application.command.LessonGroupUpdateCommand;
import com.everrefine.elms.application.dto.LessonGroupDto;
import java.util.UUID;

/** レッスングループアプリケーションサービスのインターフェース。 */
public interface LessonGroupApplicationService {

  /**
   * レッスングループを作成する。
   *
   * @param lessonGroupCreateCommand レッスングループ作成Command
   * @return 作成したレッスングループDTO
   */
  LessonGroupDto createLessonGroup(LessonGroupCreateCommand lessonGroupCreateCommand);

  /**
   * レッスングループを更新する。
   *
   * @param lessonGroupUpdateCommand レッスングループ更新Command
   * @return 更新したレッスングループDTO
   */
  LessonGroupDto updateLessonGroup(LessonGroupUpdateCommand lessonGroupUpdateCommand);

  /**
   * IDでレッスングループを削除する。
   *
   * @param lessonGroupId レッスングループID
   */
  void deleteLessonGroupById(UUID lessonGroupId);
}
