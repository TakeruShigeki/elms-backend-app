package com.everrefine.elms.domain.model.news;

import com.everrefine.elms.domain.model.PagerForRequest;
import java.time.LocalDate;

/** お知らせ検索条件の値オブジェクト。 */
public record NewsSearchCondition(
    PagerForRequest pagerForRequest,
    String title,
    LocalDate createdDateFrom,
    LocalDate createdDateTo) {

  /**
   * お知らせ検索条件を作成する。
   *
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @param title タイトル（nullまたは空文字の場合は絞り込みなし）
   * @param createdDateFrom 作成日From（nullの場合は絞り込みなし）
   * @param createdDateTo 作成日To（nullの場合は絞り込みなし）
   */
  public NewsSearchCondition(
      int pageNum, int pageSize, String title, LocalDate createdDateFrom, LocalDate createdDateTo) {
    this(new PagerForRequest(pageNum, pageSize), title, createdDateFrom, createdDateTo);
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
