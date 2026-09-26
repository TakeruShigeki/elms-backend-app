package com.everrefine.elms.domain.model.lesson;

import com.everrefine.elms.domain.model.PagerForRequest;
import jakarta.annotation.Nullable;
import java.time.LocalDate;

/** レッスン検索条件の値オブジェクト。 */
public record LessonSearchCriteria(
    PagerForRequest pagerForRequest,
    @Nullable String courseId,
    @Nullable String lessonGroupId,
    @Nullable String title,
    @Nullable LocalDate createdDateFrom,
    @Nullable LocalDate createdDateTo) {

  /**
   * レッスン検索条件を作成する。
   *
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @param courseId コースID（nullの場合は絞り込みなし）
   * @param lessonGroupId レッスングループID（nullの場合は絞り込みなし）
   * @param title タイトル（nullの場合は絞り込みなし）
   * @param createdDateFrom 作成日From（nullの場合は絞り込みなし）
   * @param createdDateTo 作成日To（nullの場合は絞り込みなし）
   */
  public LessonSearchCriteria(
      int pageNum,
      int pageSize,
      String courseId,
      String lessonGroupId,
      String title,
      LocalDate createdDateFrom,
      LocalDate createdDateTo) {
    this(
        new PagerForRequest(pageNum, pageSize),
        courseId,
        lessonGroupId,
        title,
        createdDateFrom,
        createdDateTo);
  }

  /**
   * 1ページ当たりの件数を返す。
   *
   * @return 1ページ当たりの件数
   */
  public int getPageSize() {
    return pagerForRequest.pageSize();
  }

  /**
   * DBクエリ用のオフセット値を返す。
   *
   * @return オフセット値
   */
  public int getOffset() {
    return pagerForRequest.getOffset();
  }
}
