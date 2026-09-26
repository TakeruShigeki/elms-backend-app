package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.news.NewsSearchCondition;
import java.time.LocalDate;

/** お知らせ検索用のコマンド。 */
public record NewsSearchCommand(
    int pageNum, int pageSize, String title, LocalDate createdDateFrom, LocalDate createdDateTo) {

  /**
   * NewsSearchConditionに変換する。
   *
   * @return お知らせ検索条件
   */
  public NewsSearchCondition toSearchCondition() {
    return new NewsSearchCondition(pageNum, pageSize, title, createdDateFrom, createdDateTo);
  }
}
