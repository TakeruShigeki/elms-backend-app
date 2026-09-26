package com.everrefine.elms.domain.model.lesson;

import com.everrefine.elms.domain.exception.InvalidValueException;

/** 動画URLの値オブジェクト。 レッスンの動画URLとして使うことを想定している。 */
public record VideoUrl(String value) {

  public static final int MAX_LENGTH = 2048;

  /**
   * 動画URLの値オブジェクトを生成する。
   *
   * @param value 動画のURL文字列
   */
  public VideoUrl {
    if (value == null) {
      throw new InvalidValueException("動画URLはnullにできません");
    }

    if (value.length() > MAX_LENGTH) {
      throw new InvalidValueException("動画URLは" + MAX_LENGTH + "文字以内で入力してください");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
