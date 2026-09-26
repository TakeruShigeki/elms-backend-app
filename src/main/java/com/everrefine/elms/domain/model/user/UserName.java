package com.everrefine.elms.domain.model.user;

import com.everrefine.elms.domain.exception.InvalidValueException;
import java.util.regex.Pattern;

/** ユーザー名の値オブジェクト。 */
public record UserName(String value) {

  // 最大文字数
  private static final int MAX_LENGTH = 50;
  // 全角文字が含まれていないことを確認する正規表現
  private static final Pattern NON_FULL_WIDTH_PATTERN = Pattern.compile("^[\u0000-\u00ff]*$");

  /**
   * ユーザー名を作成する。
   *
   * @param value ユーザー名文字列（50文字以内、半角のみ）
   */
  public UserName {
    if (value == null || value.length() > MAX_LENGTH) {
      throw new InvalidValueException("ユーザー名は" + MAX_LENGTH + "文字以内で入力してください");
    }

    if (!NON_FULL_WIDTH_PATTERN.matcher(value).matches()) {
      throw new InvalidValueException("ユーザー名に全角文字は使用できません");
    }
  }
}
