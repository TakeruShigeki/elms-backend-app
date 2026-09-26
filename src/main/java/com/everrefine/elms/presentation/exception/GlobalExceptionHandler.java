package com.everrefine.elms.presentation.exception;

import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.exception.InvalidValueException;
import com.everrefine.elms.presentation.response.ErrorResponse;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * グローバル例外ハンドラー。
 *
 * <p>{@link ResponseEntityExceptionHandler} を継承することで、Spring MVCが投げる標準例外（未対応のHTTPメソッド、
 * 不正なContent-Type、存在しないURLなど）が個別のハンドラを書かなくても適切なステータスで返る。継承していないと、
 * それらがすべて末尾のcatch-allに落ちて500になってしまう。
 *
 * <p>親クラスの既定レスポンスは {@code ProblemDetail} 形式のため、{@link #handleExceptionInternal} を上書きして 本アプリの {@link
 * ErrorResponse} 形式（code / message）に統一している。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /** ステータスごとのクライアント向けメッセージ。内部情報を露出させないため定型文を返す。 */
  private static final Map<HttpStatus, String> MESSAGES =
      Map.of(
          HttpStatus.BAD_REQUEST, "リクエストの形式が不正です",
          HttpStatus.NOT_FOUND, "リソースが見つかりません",
          HttpStatus.METHOD_NOT_ALLOWED, "このHTTPメソッドはサポートされていません",
          HttpStatus.NOT_ACCEPTABLE, "サポートされていないレスポンス形式が要求されました",
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, "サポートされていないContent-Typeです",
          HttpStatus.PAYLOAD_TOO_LARGE, "ファイルサイズが大きすぎます",
          HttpStatus.INTERNAL_SERVER_ERROR, "サーバーエラーが発生しました");

  /**
   * Spring MVC標準例外のレスポンスを、本アプリの {@link ErrorResponse} 形式に差し替える。
   *
   * @param ex 例外
   * @param body 親クラスが用意したボディ（ProblemDetail。ここでは使わない）
   * @param headers レスポンスヘッダー
   * @param statusCode ステータスコード
   * @param request リクエスト
   * @return エラーレスポンス
   */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    HttpStatus status = HttpStatus.valueOf(statusCode.value());
    log.warn("リクエストを処理できませんでした: status={}, {}", status.value(), ex.getMessage());
    return new ResponseEntity<>(toErrorResponse(status), headers, statusCode);
  }

  // ① リクエストボディのバリデーション失敗 → 400
  /**
   * リクエストボディのバリデーション失敗を処理する。どの項目が不正かを返すため親クラスの既定動作を上書きする。
   *
   * @param ex バリデーション例外
   * @param headers レスポンスヘッダー
   * @param status ステータスコード
   * @param request リクエスト
   * @return エラーレスポンス
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return new ResponseEntity<>(new ErrorResponse("VALIDATION_ERROR", message), headers, status);
  }

  // ② パスパラメータ・クエリパラメータのバリデーション失敗 → 400
  /**
   * パスパラメータ・クエリパラメータのバリデーション失敗を処理する。違反内容を返すため親クラスの既定動作を上書きする。
   *
   * @param ex バリデーション例外
   * @param headers レスポンスヘッダー
   * @param status ステータスコード
   * @param request リクエスト
   * @return エラーレスポンス
   */
  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message =
        ex.getParameterValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream())
            .map(MessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
    return new ResponseEntity<>(new ErrorResponse("VALIDATION_ERROR", message), headers, status);
  }

  // ③ パスパラメータ・クエリパラメータの型変換失敗 → 400
  /**
   * パスパラメータ・クエリパラメータの型変換失敗を処理する。
   *
   * <p>不正なUUIDなどクライアント起因の誤りであり、どのパラメータが問題かを返す。
   *
   * @param e 型変換失敗例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return new ErrorResponse("VALIDATION_ERROR", "パラメータ '" + e.getName() + "' の形式が不正です");
  }

  // ④ クライアント入力不正 → 400
  /**
   * クライアント入力不正を処理する。
   *
   * @param e 入力不正例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleBadRequest(BadRequestException e) {
    return new ErrorResponse("BAD_REQUEST", e.getMessage());
  }

  // ⑤ リソース未検出 → 404
  /**
   * リソース未検出例外を処理する。
   *
   * @param e リソース未検出例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(ResourceNotFoundException e) {
    return new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage());
  }

  // ⑥ ステータス指定付きの例外 → 指定されたステータス
  /**
   * {@link ResponseStatusException} を、指定されたステータスのまま返す。
   *
   * <p>catch-allより先に {@code @ExceptionHandler} が解決されるため、このハンドラがないと 400/401 を意図した例外がすべて500になってしまう。
   *
   * @param e ステータス指定付き例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
    HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
    String message = e.getReason() != null ? e.getReason() : toErrorResponse(status).message();
    return ResponseEntity.status(status).body(new ErrorResponse(status.name(), message));
  }

  // ⑦ 権限不足 → 403
  /**
   * 認証済みだが権限が足りない場合を処理する。
   *
   * <p>{@code @PreAuthorize} による拒否はコントローラー呼び出し中に発生するため、Spring Securityのフィルタではなく
   * このハンドラに到達する。ここで拾わないとcatch-allに落ちて500になる。
   *
   * @param e アクセス拒否例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorResponse handleAccessDenied(AccessDeniedException e) {
    return new ErrorResponse("FORBIDDEN", "この操作を行う権限がありません");
  }

  // ⑧ DBデータ不整合（値オブジェクト生成失敗） → 500
  /**
   * 不正値例外を処理する。
   *
   * @param e 不正値例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(InvalidValueException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleInvalidValue(InvalidValueException e) {
    log.error("DBのデータから値オブジェクトを生成できませんでした", e);
    return new ErrorResponse(
        "INVALID_VALUE", toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR).message());
  }

  // ⑨ 想定外のすべて → 500
  /**
   * 想定外の例外を処理する。
   *
   * <p>例外の詳細はログにのみ出力する。レスポンスに含めると内部構造やIDが露出するため定型文を返す。
   *
   * @param e 例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleGeneral(Exception e) {
    log.error("想定外のエラーが発生しました", e);
    return new ErrorResponse(
        "INTERNAL_ERROR", toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR).message());
  }

  /**
   * ステータスに対応するクライアント向けのエラーレスポンスを組み立てる。
   *
   * @param status HTTPステータス
   * @return エラーレスポンス
   */
  private ErrorResponse toErrorResponse(HttpStatus status) {
    return new ErrorResponse(
        status.name(), MESSAGES.getOrDefault(status, status.getReasonPhrase()));
  }
}
