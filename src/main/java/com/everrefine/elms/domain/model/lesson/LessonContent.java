package com.everrefine.elms.domain.model.lesson;

import com.everrefine.elms.domain.exception.InvalidValueException;

/** レッスン本文の値オブジェクト。 */
public record LessonContent(String value) {

  // 最大文字数
  private static final int MAX_LENGTH = 1_000_000;

  /**
   * レッスン本文を作成する。
   *
   * @param value 本文文字列（1,000,000文字以内）
   */
  public LessonContent {
    if (value == null || value.length() > MAX_LENGTH) {
      throw new InvalidValueException("本文は" + MAX_LENGTH + "文字以内で入力してください");
    }
  }
}
