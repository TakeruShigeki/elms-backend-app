package com.everrefine.elms.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

@Service
public class JwtApplicationServiceImpl implements JwtApplicationService {

  // Bearerトークンの先頭文字列
  private static final String BEARER_PREFIX = "Bearer ";
  // JWTの有効期限（1分）: Cookie の maxAge と揃えることで、期限切れJWT → RefreshToken再発行の経路を正しく活かす
  private static final Duration ACCESS_TOKEN_EXPIRATION = Duration.ofMinutes(1);
  // リフレッシュトークンの有効期限（30日）
  private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(30);

  // 256ビット（32バイト）のBase64エンコードされた秘密鍵
  private final String base64EncodedSecretKey;
  // Cookieのセキュアフラグ（本番はtrue、開発はfalse）
  private final boolean cookieSecure;

  public JwtApplicationServiceImpl(
      @Value("${jwt.secret}") String base64EncodedSecretKey,
      @Value("${cookie.secure:false}") boolean cookieSecure) {
    this.base64EncodedSecretKey = base64EncodedSecretKey;
    this.cookieSecure = cookieSecure;
  }

  /**
   * 署名キーを取得する。
   *
   * @return 署名キー
   */
  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * トークンの中からSubject（ユーザーID）を取得する。
   *
   * @param token 対象のトークン
   * @return Subject（ユーザーID）
   */
  @Override
  public String extractSubjectFromToken(String token) {
    return extractSpecificInfoFromClaim(token, Claims::getSubject);
  }

  /**
   * クレーム（情報）内の指定の情報を抽出する
   *
   * @param token トークン
   * @param claimsTFunction 引数: クレーム、戻り値: TのFunction
   * @param <T> クレーム（情報）内の指定の情報の型
   * @return クレーム（情報）内の指定の情報
   */
  @Override
  public <T> T extractSpecificInfoFromClaim(String token, Function<Claims, T> claimsTFunction) {
    try {
      Claims claims =
          Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
      return claimsTFunction.apply(claims);
    } catch (ExpiredJwtException e) {
      // 期限切れトークンでもクレーム自体は取得できる
      return claimsTFunction.apply(e.getClaims());
    }
  }

  /**
   * JWTトークンをレスポンスのクッキーにセットする。
   *
   * @param response レスポンス情報
   * @param token JWTトークン
   */
  @Override
  public void setJwtTokenToResponseCookie(HttpServletResponse response, String token) {
    ResponseCookie jwtCookie =
        ResponseCookie.from("JWT", token)
            .httpOnly(false) // フロントエンドでクッキーのJWTをリクエストヘッダーに付与するため、JSで扱えるようにfalseにする。
            .secure(cookieSecure)
            .path("/")
            // JWT Cookie の寿命（1分）。JWT トークン本体も1分に統一し盗用リスクを最小化する。
            .maxAge(Duration.ofMinutes(1))
            // sameSite・・・「どんなときにブラウザがこのCookieを送るか」を決める設定。
            // Strict（CSRF対策）: 他ドメインorポートの場合は送らない。ただし、リクエストヘッダーでJWTを指定した場合は送られる。
            // Lax：GETの場合だけ送る。
            // None: 無条件に送る。違うドメインやポートに送る場合はNoneでないといけない。
            .sameSite("Strict") // CSRF対策用で、リクエストヘッダーJWT無しのリクエスト時は、クッキーではJWTを送らないようにする。
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
  }

  /**
   * リフレッシュトークンをレスポンスのクッキーにセットする。
   *
   * @param response レスポンス情報
   * @param token リフレッシュトークン
   * @param rememberMe trueの場合は30日の永続Cookie、falseの場合はセッションCookie（ブラウザを閉じると削除）
   */
  @Override
  public void setRefreshTokenToResponseCookie(
      HttpServletResponse response, String token, boolean rememberMe) {
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from("RefreshToken", token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            // SameSite=None は Secure=true が必須（Chrome 80+）。HTTP のローカル開発では Lax を使用する。
            .sameSite(cookieSecure ? "None" : "Lax");

    if (rememberMe) {
      builder.maxAge(Duration.ofDays(30));
    }
    // rememberMe=falseの場合はmaxAgeを指定しない（セッションCookie）

    response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
  }

  /**
   * JWTとRefreshTokenを削除する。（ログアウト用）
   *
   * @param response レスポンス情報
   */
  @Override
  public void clearJwtAndRefreshToken(HttpServletResponse response) {
    ResponseCookie jwtCookie =
        ResponseCookie.from("JWT", "")
            .httpOnly(false) // 設定時（httpOnly=false）と統一
            .secure(cookieSecure)
            .path("/")
            .maxAge(0)
            .build();
    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("RefreshToken", "")
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(0)
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
  }

  /**
   * リクエストヘッダーからJWTを取得する。
   *
   * @param request リクエスト情報
   * @return JWTの値
   */
  @Override
  public Optional<String> getJwtFromRequestHeader(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
      return Optional.of(authorizationHeader.substring(BEARER_PREFIX.length()));
    }
    return Optional.empty();
  }

  /**
   * トークンのフォーマットが有効であるかをチェックする。
   *
   * @param token 対象のトークン
   * @return true: トークンのフォーマットが有効である、false: トークンのフォーマットが無効である
   */
  @Override
  public boolean isTokenFormatValid(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException e) {
      return true; // 期限切れはフォーマット自体は正常
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * トークンが有効期限切れであるかをチェックする。
   *
   * @param token 対象トークン
   * @return true: 有効期限切れ、false: 有効期間中
   */
  @Override
  public boolean isTokenExpired(String token) {
    Date expiration = extractSpecificInfoFromClaim(token, Claims::getExpiration);
    return expiration.before(new Date());
  }

  /**
   * 認証情報をSecurityContextにセットする。
   *
   * @param request リクエスト情報
   * @param userDetails 認証情報
   */
  @Override
  public void setTokenToSecurityContext(HttpServletRequest request, UserDetails userDetails) {
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  /**
   * クッキーからRefreshTokenを取得する。 クッキー自体がない or RefreshTokenのクッキーがない場合はOptional.empty()を返す。
   *
   * @param request リクエスト情報
   * @return RefreshTokenのクッキー
   */
  @Override
  public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }

    for (Cookie cookie : cookies) {
      if ("RefreshToken".equals(cookie.getName())) {
        String value = cookie.getValue();
        if (value != null && !value.isEmpty()) {
          return Optional.of(value);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * JWTトークンを生成する。
   *
   * @param subject サブジェクト
   * @return JWTトークン
   */
  @Override
  public String generateJwtToken(String subject) {
    return generateToken(subject, ACCESS_TOKEN_EXPIRATION);
  }

  /**
   * リフレッシュトークンを生成する。rememberMeの値をトークン内に埋め込み、更新時に引き継げるようにする。
   *
   * @param subject サブジェクト
   * @param rememberMe ログイン状態を保持するか
   * @return リフレッシュトークン
   */
  @Override
  public String generateRefreshToken(String subject, boolean rememberMe) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(subject) // 「誰のトークンか」= ユーザーID
        .setIssuedAt(Date.from(now)) // 「いつ発行したか」
        .setExpiration(Date.from(now.plus(REFRESH_TOKEN_EXPIRATION))) // 「いつまで有効か」= 30日後
        .claim("rememberMe", rememberMe) // rememberMe の値を埋め込む（トークン更新時に引き継ぐため）
        .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 秘密鍵で署名（改ざん防止）
        .compact(); // 文字列化して返す
  }

  /**
   * リフレッシュトークンからrememberMeの値を取得する。
   *
   * @param token リフレッシュトークン
   * @return rememberMeの値（クレームが存在しない場合はfalse）
   */
  @Override
  public boolean getRememberMeFromToken(String token) {
    return extractSpecificInfoFromClaim(
        token,
        claims -> {
          Boolean rememberMe = claims.get("rememberMe", Boolean.class);
          return rememberMe != null && rememberMe;
        });
  }

  /**
   * トークンを生成する。
   *
   * @param subject サブジェクト
   * @param expiration トークンの有効期限
   * @return トークン
   */
  private String generateToken(String subject, Duration expiration) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(subject) // 「誰のトークンか」= ユーザーID
        .setIssuedAt(Date.from(now)) // 「いつ発行したか」
        .setExpiration(Date.from(now.plus(expiration))) // 「いつまで有効か」
        .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 秘密鍵で署名（改ざん防止）
        .compact(); // 文字列化して返す
  }
}
