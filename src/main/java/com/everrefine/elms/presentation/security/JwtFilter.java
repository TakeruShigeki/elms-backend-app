package com.everrefine.elms.presentation.security;

import com.everrefine.elms.application.service.JwtApplicationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** JWTによる認証フィルター。 */
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final CustomUserDetailsService customUserDetailsService;
  private final JwtApplicationService jwtApplicationService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // リクエストヘッダーからJWTを取得する。
    Optional<String> jwtOptional = jwtApplicationService.getJwtFromRequestHeader(request);

    if (jwtOptional.isPresent()) {
      // リクエストヘッダーにJWTが付与されている場合
      String jwt = jwtOptional.get();

      if (jwtApplicationService.isTokenFormatValid(jwt)) {
        // リクエストヘッダーJWTの値のフォーマットが正常である場合
        // JWTからユーザーIDを取得する。
        String userId = jwtApplicationService.extractSubjectFromToken(jwt);

        UserDetails userDetails;
        try {
          userDetails = customUserDetailsService.loadUserById(userId);
        } catch (UsernameNotFoundException e) {
          // JWT内のユーザーがデータソースに存在しない場合
          jwtApplicationService.clearJwtAndRefreshToken(response);
          filterChain.doFilter(request, response);
          return; // SecurityContextに認証情報を設定しないため、APIとしては401となる。
        }

        if (jwtApplicationService.isTokenExpired(jwt)) {
          // JWTの有効期限が切れた場合：ロード済みのuserDetailsを渡してDB再検索を省略
          recreateJwtAndRefreshTokenWithKnownUser(request, response, userId, userDetails);
          filterChain.doFilter(request, response);
          return;
        }

        // JWTがすべてのチェックをクリアした場合
        jwtApplicationService.setTokenToSecurityContext(request, userDetails);
      } else {
        // リクエストヘッダーJWTの値のフォーマットが不正（改ざん・署名不正等）の場合
        if (jwtApplicationService.getRefreshTokenFromCookie(request).isPresent()) {
          recreateJwtAndRefreshToken(request, response);
        } else {
          jwtApplicationService.clearJwtAndRefreshToken(response);
        }
      }
    } else {
      // リクエストヘッダーにJWTが付与されていない場合、RefreshTokenで再認証を試みる
      if (jwtApplicationService.getRefreshTokenFromCookie(request).isPresent()) {
        recreateJwtAndRefreshToken(request, response);
      } else {
        jwtApplicationService.clearJwtAndRefreshToken(response);
      }
    }

    filterChain.doFilter(request, response);
  }

  /**
   * JWT期限切れ時用。ロード済みの userDetails を再利用してDB検索を省略しトークンを再生成する。
   *
   * @param request リクエスト情報
   * @param response レスポンス情報
   * @param userId JWTから取得済みのユーザーID
   * @param userDetails ロード済みの認証情報
   */
  private void recreateJwtAndRefreshTokenWithKnownUser(
      HttpServletRequest request,
      HttpServletResponse response,
      String userId,
      UserDetails userDetails) {
    jwtApplicationService
        .getRefreshTokenFromCookie(request)
        .ifPresentOrElse(
            refreshToken -> {
              if (!jwtApplicationService.isTokenFormatValid(refreshToken)) {
                jwtApplicationService.clearJwtAndRefreshToken(response);
                return;
              }
              if (jwtApplicationService.isTokenExpired(refreshToken)) {
                // JWTだけでなくリフレッシュトークンも期限切れというのは、30日間未使用の時くらいしか起こり得ない。
                jwtApplicationService.clearJwtAndRefreshToken(response);
                return;
              }
              boolean rememberMe = jwtApplicationService.getRememberMeFromToken(refreshToken);
              String newJwtToken = jwtApplicationService.generateJwtToken(userId);
              String newRefreshToken =
                  jwtApplicationService.generateRefreshToken(userId, rememberMe);
              jwtApplicationService.setJwtTokenToResponseCookie(response, newJwtToken);
              // JWTが1分で切れるたびに、RefreshTokenも新しく発行し直す。
              // つまり、アクティブに使い続けている限り、RefreshTokenの期限（30日後）は延長され続ける。
              // rememberMe=true であれば、毎日使っているユーザーは永遠にログイン状態が続く。これは「スライディングセッション」と呼ばれるパターン
              jwtApplicationService.setRefreshTokenToResponseCookie(
                  response, newRefreshToken, rememberMe);
              jwtApplicationService.setTokenToSecurityContext(request, userDetails);
            },
            () -> jwtApplicationService.clearJwtAndRefreshToken(response));
  }

  /**
   * JWTとリフレッシュトークンを再生成する。
   *
   * @param request リクエスト情報
   * @param response レスポンス情報
   */
  private void recreateJwtAndRefreshToken(
      HttpServletRequest request, HttpServletResponse response) {
    // JWTが無効（ユーザーIDが存在しないor有効期限切れ）であれば、リフレッシュトークンを使って新しいJWTを発行する。
    jwtApplicationService
        .getRefreshTokenFromCookie(request)
        .ifPresent(
            refreshToken -> {
              if (!jwtApplicationService.isTokenFormatValid(refreshToken)) {
                // リフレッシュトークンのフォーマットが不正（改ざん等）の場合
                jwtApplicationService.clearJwtAndRefreshToken(response);
                return;
              }

              // リフレッシュトークンのフォーマットが正常である場合
              String userId = jwtApplicationService.extractSubjectFromToken(refreshToken);

              UserDetails userDetails;
              try {
                userDetails = customUserDetailsService.loadUserById(userId);
              } catch (UsernameNotFoundException e) {
                // JWT内のユーザーがデータソースに存在しない場合
                jwtApplicationService.clearJwtAndRefreshToken(response);
                return;
              }

              if (jwtApplicationService.isTokenExpired(refreshToken)) {
                // RefreshTokenの有効期限が切れた場合
                jwtApplicationService.clearJwtAndRefreshToken(response);
                return;
              }

              // 既存のRefreshTokenからrememberMeを取得し、新しいトークンに引き継ぐ
              boolean rememberMe = jwtApplicationService.getRememberMeFromToken(refreshToken);

              // JWTとRefreshTokenを再生成＆クッキーにセットする。
              String newJwtToken = jwtApplicationService.generateJwtToken(userId);
              String newRefreshToken =
                  jwtApplicationService.generateRefreshToken(userId, rememberMe);
              jwtApplicationService.setJwtTokenToResponseCookie(response, newJwtToken);
              jwtApplicationService.setRefreshTokenToResponseCookie(
                  response, newRefreshToken, rememberMe);

              // 1回目のloadUserByIdの結果を再利用してSecurityContextに認証情報をセット
              jwtApplicationService.setTokenToSecurityContext(request, userDetails);
            });
  }
}
