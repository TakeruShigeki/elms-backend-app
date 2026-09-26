package com.everrefine.elms.domain.model.lesson;

import com.everrefine.elms.domain.exception.InvalidValueException;

/** レッスン名の値オブジェクト。 */
public record LessonTitle(String value) {

  // 最大文字数
  private static final int MAX_LENGTH = 255;

  /**
   * レッスン名を作成する。
   *
   * @param value レッスン名文字列（255文字以内）
   */
  public LessonTitle {
    if (value == null || value.length() > MAX_LENGTH) {
      throw new InvalidValueException("タイトルは" + MAX_LENGTH + "文字以内で入力してください");
    }
  }
}
