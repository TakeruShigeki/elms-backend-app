package com.everrefine.elms.application.exception;

/** クライアントからの入力値が不正な場合に発生する例外。 */
public class BadRequestException extends RuntimeException {

  /**
   * 不正なリクエストに関する例外を生成する。
   *
   * @param message エラーメッセージ
   */
  public BadRequestException(String message) {
    super(message);
  }

  /**
   * 不正なリクエストに関する例外を生成する。
   *
   * @param message エラーメッセージ
   * @param cause 原因例外
   */
  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
