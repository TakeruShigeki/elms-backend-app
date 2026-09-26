package com.everrefine.elms.domain.model.news;

import com.everrefine.elms.domain.exception.InvalidValueException;

/** お知らせ本文の値オブジェクト。 */
public record NewsContent(String value) {

  // 最大文字数
  private static final int MAX_LENGTH = 1_000_000;

  /**
   * お知らせ本文を作成する。
   *
   * @param value 本文文字列（1,000,000文字以内）
   */
  public NewsContent {
    if (value == null || value.length() > MAX_LENGTH) {
      throw new InvalidValueException("本文は" + MAX_LENGTH + "文字以内で入力してください");
    }
  }
}
