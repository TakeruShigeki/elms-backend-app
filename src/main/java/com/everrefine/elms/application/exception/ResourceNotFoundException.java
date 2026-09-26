package com.everrefine.elms.application.exception;

/** リソースが見つからない場合にスローされる例外。 */
public class ResourceNotFoundException extends RuntimeException {

  /**
   * リソースが見つからない場合の例外を生成する。
   *
   * @param resourceClass リソースのクラス
   * @param id リソースID
   */
  public ResourceNotFoundException(Class<?> resourceClass, String id) {
    super(resourceClass.getSimpleName() + " が見つかりませんでした。id = " + id);
  }

  /**
   * 原因例外付きでリソースが見つからない場合の例外を生成する。
   *
   * @param resourceClass リソースのクラス
   * @param id リソースID
   * @param cause 原因例外
   */
  public ResourceNotFoundException(Class<?> resourceClass, String id, Throwable cause) {
    super(resourceClass.getSimpleName() + " が見つかりませんでした。id = " + id, cause);
  }
}
