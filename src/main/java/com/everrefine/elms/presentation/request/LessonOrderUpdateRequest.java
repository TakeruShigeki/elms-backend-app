package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.LessonOrderUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** レッスン並び順更新リクエスト。 */
public record LessonOrderUpdateRequest(
    @Schema(description = "移動先の直前となるレッスンID（先頭に移動する場合はnull）", example = "3") UUID precedingLessonId,
    @Schema(description = "移動先の直後となるレッスンID（末尾に移動する場合はnull）", example = "5")
        UUID followingLessonId) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @param lessonId レッスンID
   * @return レッスン並び順更新Command
   */
  public LessonOrderUpdateCommand toCommand(UUID lessonId) {
    return new LessonOrderUpdateCommand(lessonId, precedingLessonId, followingLessonId);
  }
}
