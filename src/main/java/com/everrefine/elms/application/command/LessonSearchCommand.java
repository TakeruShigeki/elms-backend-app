package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.lesson.LessonSearchCriteria;
import jakarta.annotation.Nullable;
import java.time.LocalDate;

/** レッスン検索用のコマンド。 */
public record LessonSearchCommand(
    int pageNum,
    int pageSize,
    @Nullable String courseId,
    @Nullable String lessonGroupId,
    @Nullable String title,
    @Nullable LocalDate createdDateFrom,
    @Nullable LocalDate createdDateTo) {

  /**
   * LessonSearchCriteriaに変換する。
   *
   * @return レッスン検索条件
   */
  public LessonSearchCriteria toCriteria() {
    return new LessonSearchCriteria(
        pageNum, pageSize, courseId, lessonGroupId, title, createdDateFrom, createdDateTo);
  }
}
