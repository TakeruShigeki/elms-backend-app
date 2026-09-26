package com.everrefine.elms.domain.model;

import com.everrefine.elms.domain.exception.InvalidValueException;

/** サムネイル画像URLの値オブジェクト。 ユーザー・コースのサムネイル画像URLとして使うことを想定している。 */
public record ThumbnailUrl(String value) {

  public static final int MAX_LENGTH = 2048;

  /**
   * サムネイル画像URLの値オブジェクトを生成する。
   *
   * @param value サムネイル画像のURL文字列
   */
  public ThumbnailUrl {
    if (value == null) {
      throw new InvalidValueException("サムネイルURLはnullにできません");
    }

    if (value.length() > MAX_LENGTH) {
      throw new InvalidValueException("サムネイルURLは" + MAX_LENGTH + "文字以内で入力してください");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
