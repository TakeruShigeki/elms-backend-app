package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.LessonSearchCommand;
import jakarta.annotation.Nullable;
import java.time.LocalDate;

/** レッスン検索リクエスト。 */
public record LessonSearchRequest(
    @Nullable Integer pageNum,
    @Nullable Integer pageSize,
    @Nullable String courseId,
    @Nullable String lessonGroupId,
    @Nullable String title,
    @Nullable LocalDate createdDateFrom,
    @Nullable LocalDate createdDateTo) {

  /**
   * Commandオブジェクトに変換する。ページ番号・件数が未指定の場合はデフォルト値（1 / 10）を適用する。
   *
   * @return レッスン検索Command
   */
  public LessonSearchCommand toCommand() {
    return new LessonSearchCommand(
        pageNum == null ? 1 : pageNum,
        pageSize == null ? 10 : pageSize,
        courseId,
        lessonGroupId,
        title,
        createdDateFrom,
        createdDateTo);
  }
}
