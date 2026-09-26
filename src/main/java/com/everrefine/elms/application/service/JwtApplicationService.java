package com.everrefine.elms.application.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;

/** JWT トークンの生成・検証・Cookie 操作を行うアプリケーションサービス。 */
public interface JwtApplicationService {

  /**
   * トークンからサブジェクト（メールアドレス）を取得する。
   *
   * @param token JWTトークン
   * @return サブジェクト
   */
  String extractSubjectFromToken(String token);

  /**
   * トークンからクレーム情報を取得する。
   *
   * @param <T> クレームの型
   * @param token JWTトークン
   * @param claimsTFunction クレーム抽出関数
   * @return クレーム情報
   */
  <T> T extractSpecificInfoFromClaim(String token, Function<Claims, T> claimsTFunction);

  /**
   * JWTトークンをレスポンスCookieにセットする。
   *
   * @param response HTTPレスポンス
   * @param token JWTトークン
   */
  void setJwtTokenToResponseCookie(HttpServletResponse response, String token);

  /**
   * リフレッシュトークンをレスポンスCookieにセットする。
   *
   * @param response HTTPレスポンス
   * @param token リフレッシュトークン
   * @param rememberMe ログイン状態を保持するか
   */
  void setRefreshTokenToResponseCookie(
      HttpServletResponse response, String token, boolean rememberMe);

  /**
   * JWTトークンとリフレッシュトークンをCookieからクリアする。
   *
   * @param response HTTPレスポンス
   */
  void clearJwtAndRefreshToken(HttpServletResponse response);

  /**
   * リクエストヘッダーからJWTトークンを取得する。
   *
   * @param request HTTPリクエスト
   * @return JWTトークン（存在しない場合はempty）
   */
  Optional<String> getJwtFromRequestHeader(HttpServletRequest request);

  /**
   * トークンの形式が有効かどうかを検証する。
   *
   * @param token JWTトークン
   * @return 有効な場合はtrue
   */
  boolean isTokenFormatValid(String token);

  /**
   * トークンが期限切れかどうかを検証する。
   *
   * @param token JWTトークン
   * @return 期限切れの場合はtrue
   */
  boolean isTokenExpired(String token);

  /**
   * トークンをセキュリティコンテキストにセットする。
   *
   * @param request HTTPリクエスト
   * @param userDetails ユーザー詳細情報
   */
  void setTokenToSecurityContext(HttpServletRequest request, UserDetails userDetails);

  /**
   * CookieからリフレッシュトークンをOptionalで取得する。
   *
   * @param request HTTPリクエスト
   * @return リフレッシュトークン（存在しない場合はempty）
   */
  Optional<String> getRefreshTokenFromCookie(HttpServletRequest request);

  /**
   * JWTトークンを生成する。
   *
   * @param subject サブジェクト（メールアドレス）
   * @return JWTトークン
   */
  String generateJwtToken(String subject);

  /**
   * リフレッシュトークンを生成する。
   *
   * @param subject サブジェクト（メールアドレス）
   * @param rememberMe ログイン状態を保持するか
   * @return リフレッシュトークン
   */
  String generateRefreshToken(String subject, boolean rememberMe);

  /**
   * トークンからrememberMeフラグを取得する。
   *
   * @param token JWTトークン
   * @return rememberMeフラグ
   */
  boolean getRememberMeFromToken(String token);
}
