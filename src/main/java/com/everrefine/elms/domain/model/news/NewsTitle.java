package com.everrefine.elms.domain.model.news;

import com.everrefine.elms.domain.exception.InvalidValueException;

/** ニュースの値オブジェクト。 */
public record NewsTitle(String value) {

  // 最大文字数
  private static final int MAX_LENGTH = 255;

  /**
   * お知らせタイトルを作成する。
   *
   * @param value タイトル文字列（255文字以内）
   */
  public NewsTitle {
    if (value == null || value.length() > MAX_LENGTH) {
      throw new InvalidValueException("タイトルは" + MAX_LENGTH + "文字以内で入力してください");
    }
  }
}
