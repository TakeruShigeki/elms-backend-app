package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Testcontainers
class JwtApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  private static final String SUBJECT = "550e8400-e29b-41d4-a716-446655440000";

  @Autowired private JwtApplicationService jwtApplicationService;

  /** 指定した名前のSet-Cookieヘッダーを取得する。 */
  private String findSetCookieHeader(MockHttpServletResponse response, String cookieName) {
    List<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
    return headers.stream()
        .filter(header -> header.startsWith(cookieName + "="))
        .findFirst()
        .orElseThrow(() -> new AssertionError(cookieName + " のSet-Cookieヘッダーがありません: " + headers));
  }

  @Nested
  class トークン生成 {

    @Test
    void アクセストークンからサブジェクトを取り出せる() {
      // Act
      String token = jwtApplicationService.generateJwtToken(SUBJECT);

      // Assert
      assertNotNull(token);
      assertEquals(SUBJECT, jwtApplicationService.extractSubjectFromToken(token));
      assertTrue(jwtApplicationService.isTokenFormatValid(token));
      assertFalse(jwtApplicationService.isTokenExpired(token));
    }

    @Test
    void リフレッシュトークンからサブジェクトを取り出せる() {
      // Act
      String token = jwtApplicationService.generateRefreshToken(SUBJECT, false);

      // Assert
      assertEquals(SUBJECT, jwtApplicationService.extractSubjectFromToken(token));
      assertFalse(jwtApplicationService.isTokenExpired(token));
    }

    /** rememberMeはトークンのクレームとして保持される。 */
    @Test
    void リフレッシュトークンにrememberMeが保持される() {
      // Act
      String rememberMeToken = jwtApplicationService.generateRefreshToken(SUBJECT, true);
      String sessionToken = jwtApplicationService.generateRefreshToken(SUBJECT, false);

      // Assert
      assertTrue(jwtApplicationService.getRememberMeFromToken(rememberMeToken));
      assertFalse(jwtApplicationService.getRememberMeFromToken(sessionToken));
    }

    @Test
    void 改ざんされたトークンはフォーマット不正と判定される() {
      // Arrange
      String token = jwtApplicationService.generateJwtToken(SUBJECT);
      String tamperedToken = token.substring(0, token.length() - 3) + "abc";

      // Act & Assert
      assertFalse(jwtApplicationService.isTokenFormatValid(tamperedToken));
    }

    @Test
    void JWT形式でない文字列はフォーマット不正と判定される() {
      assertFalse(jwtApplicationService.isTokenFormatValid("not-a-jwt"));
    }
  }

  @Nested
  class Cookieへのセット {

    /** アクセストークンのCookieは1分（60秒）で失効する。 */
    @Test
    void アクセストークンのCookieは有効期限が1分である() {
      // Arrange
      MockHttpServletResponse response = new MockHttpServletResponse();
      String token = jwtApplicationService.generateJwtToken(SUBJECT);

      // Act
      jwtApplicationService.setJwtTokenToResponseCookie(response, token);

      // Assert
      String header = findSetCookieHeader(response, "JWT");
      assertTrue(header.contains("Max-Age=60"), () -> "有効期限が1分ではありません: " + header);
      assertTrue(header.contains("Path=/"), () -> header);
      assertTrue(header.contains("SameSite=Strict"), () -> header);
    }

    /** フロントエンドがJSからJWTを読み取ってヘッダーに付与するため、HttpOnlyにはしない。 */
    @Test
    void アクセストークンのCookieはHttpOnlyでない() {
      // Arrange
      MockHttpServletResponse response = new MockHttpServletResponse();
      String token = jwtApplicationService.generateJwtToken(SUBJECT);

      // Act
      jwtApplicationService.setJwtTokenToResponseCookie(response, token);

      // Assert
      String header = findSetCookieHeader(response, "JWT");
      assertFalse(header.contains("HttpOnly"), () -> header);
    }

    /** rememberMe=true のときだけ永続Cookie（30日）になる。 */
    @Test
    void rememberMeがtrueの場合リフレッシュトークンのCookieは30日の永続Cookieになる() {
      // Arrange
      MockHttpServletResponse response = new MockHttpServletResponse();
      String token = jwtApplicationService.generateRefreshToken(SUBJECT, true);

      // Act
      jwtApplicationService.setRefreshTokenToResponseCookie(response, token, true);

      // Assert
      String header = findSetCookieHeader(response, "RefreshToken");
      assertTrue(header.contains("Max-Age=2592000"), () -> "30日の永続Cookieではありません: " + header);
      assertTrue(header.contains("HttpOnly"), () -> header);
    }

    /** rememberMe=false のときはMax-Ageを付けず、ブラウザを閉じると破棄される。 */
    @Test
    void rememberMeがfalseの場合リフレッシュトークンのCookieはセッションCookieになる() {
      // Arrange
      MockHttpServletResponse response = new MockHttpServletResponse();
      String token = jwtApplicationService.generateRefreshToken(SUBJECT, false);

      // Act
      jwtApplicationService.setRefreshTokenToResponseCookie(response, token, false);

      // Assert
      String header = findSetCookieHeader(response, "RefreshToken");
      assertFalse(header.contains("Max-Age"), () -> "セッションCookieではありません: " + header);
      assertTrue(header.contains("HttpOnly"), () -> header);
    }

    /** rememberMeはCookieの寿命のみを変え、トークン自体の有効期限（30日）は変わらない。 */
    @Test
    void rememberMeの値によらずリフレッシュトークン自体の有効期限は変わらない() {
      // Act
      String rememberMeToken = jwtApplicationService.generateRefreshToken(SUBJECT, true);
      String sessionToken = jwtApplicationService.generateRefreshToken(SUBJECT, false);

      // Assert - どちらも失効しておらず、トークンとして有効
      assertFalse(jwtApplicationService.isTokenExpired(rememberMeToken));
      assertFalse(jwtApplicationService.isTokenExpired(sessionToken));
    }
  }

  @Nested
  class Cookieの削除 {

    @Test
    void ログアウト時に両方のCookieが失効させられる() {
      // Arrange
      MockHttpServletResponse response = new MockHttpServletResponse();

      // Act
      jwtApplicationService.clearJwtAndRefreshToken(response);

      // Assert
      assertTrue(findSetCookieHeader(response, "JWT").contains("Max-Age=0"));
      assertTrue(findSetCookieHeader(response, "RefreshToken").contains("Max-Age=0"));
    }
  }

  @Nested
  class リクエストからの取得 {

    @Test
    void AuthorizationヘッダーからJWTを取得できる() {
      // Arrange
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-value");

      // Act & Assert
      assertEquals(
          "token-value", jwtApplicationService.getJwtFromRequestHeader(request).orElseThrow());
    }

    @Test
    void Bearer形式でないAuthorizationヘッダーは取得できない() {
      // Arrange
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.addHeader(HttpHeaders.AUTHORIZATION, "token-value");

      // Act & Assert
      assertTrue(jwtApplicationService.getJwtFromRequestHeader(request).isEmpty());
    }

    @Test
    void AuthorizationヘッダーがなければJWTを取得できない() {
      assertTrue(
          jwtApplicationService.getJwtFromRequestHeader(new MockHttpServletRequest()).isEmpty());
    }

    @Test
    void Cookieからリフレッシュトークンを取得できる() {
      // Arrange
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setCookies(new Cookie("RefreshToken", "refresh-token-value"));

      // Act & Assert
      assertEquals(
          "refresh-token-value",
          jwtApplicationService.getRefreshTokenFromCookie(request).orElseThrow());
    }

    @Test
    void Cookieが空の場合はリフレッシュトークンを取得できない() {
      // Arrange - ログアウト直後は空文字のCookieが残る
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setCookies(new Cookie("RefreshToken", ""));

      // Act & Assert
      assertTrue(jwtApplicationService.getRefreshTokenFromCookie(request).isEmpty());
    }

    @Test
    void Cookieが存在しない場合はリフレッシュトークンを取得できない() {
      assertTrue(
          jwtApplicationService.getRefreshTokenFromCookie(new MockHttpServletRequest()).isEmpty());
    }

    @Test
    void 別名のCookieしかない場合はリフレッシュトークンを取得できない() {
      // Arrange
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setCookies(new Cookie("JWT", UUID.randomUUID().toString()));

      // Act & Assert
      assertTrue(jwtApplicationService.getRefreshTokenFromCookie(request).isEmpty());
    }
  }
}
