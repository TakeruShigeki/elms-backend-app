package com.everrefine.elms.domain.exception;

/** ドメインモデルの不変条件違反（値オブジェクトのバリデーション失敗など）をあらわす例外。 */
public class InvalidValueException extends RuntimeException {

  /**
   * 不正な値に関する例外を生成する。
   *
   * @param message エラーメッセージ
   */
  public InvalidValueException(String message) {
    super(message);
  }
}
