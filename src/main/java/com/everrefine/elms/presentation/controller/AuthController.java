package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.LoginHistoryCreateCommand;
import com.everrefine.elms.application.service.JwtApplicationService;
import com.everrefine.elms.application.service.UserApplicationService;
import com.everrefine.elms.presentation.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 認証に関するコントローラー。 */
@Tag(name = "認証")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtApplicationService jwtApplicationService;
  private final UserApplicationService userApplicationService;

  /** リフレッシュトークンを検証し、有効な場合はJWTを再生成する。 */
  @Operation(summary = "トークン更新", description = "リフレッシュトークンを使用してJWTを再生成します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "トークン更新成功"),
    @ApiResponse(responseCode = "401", description = "認証されていません")
  })
  @GetMapping("/refresh")
  public ResponseEntity<Void> refreshAuth() {
    // JwtFilterでRefreshTokenを検証し、有効なら新JWTを発行してSecurityContextにセットする。
    // SecurityContextが空（未認証）の場合は401を返す。
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok().build();
  }

  /**
   * ログイン処理をする。
   *
   * <p>メールアドレスとパスワードで認証し、JWTとリフレッシュトークンをCookieにセットする。
   *
   * @param loginRequest ログインリクエスト
   * @param response HTTPレスポンス（Cookie操作用）
   */
  @Operation(summary = "ログイン", description = "メールアドレスとパスワードで認証し、JWTをCookieにセットします")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "ログイン成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "メールアドレスまたはパスワードが不正")
  })
  @SecurityRequirement(name = "")
  @PostMapping("/login")
  public ResponseEntity<Void> login(
      @RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
    try {
      // authenticate()を実行すると以下が実行される。
      // ・UserDetailsService.loadUserByUsername(email)でユーザーの存在チェック
      // ・BCryptPasswordEncoder.matches()でパスワード検証
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.emailAddress(), loginRequest.password()));

      // ログイン履歴を保存する。
      LoginHistoryCreateCommand loginHistoryCreateCommand =
          loginRequest.toLoginHistoryCreateCommand();
      userApplicationService.updateUserLoginHistory(loginHistoryCreateCommand);

      // JWTとリフレッシュトークンを生成する。
      String jwtToken = jwtApplicationService.generateJwtToken(authentication.getName());
      String refreshToken =
          jwtApplicationService.generateRefreshToken(
              authentication.getName(), loginRequest.rememberMe());

      // JWTとリフレッシュトークンをクッキーに設定する。
      jwtApplicationService.setJwtTokenToResponseCookie(response, jwtToken);
      jwtApplicationService.setRefreshTokenToResponseCookie(
          response, refreshToken, loginRequest.rememberMe());

      // ログイン成功
      return ResponseEntity.ok().build();
    } catch (AuthenticationException e) {
      // 認証失敗
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  /**
   * ログアウト処理をする。
   *
   * <p>JWTとリフレッシュトークンをCookieから削除する。
   *
   * @param response HTTPレスポンス（Cookie操作用）
   */
  @Operation(summary = "ログアウト", description = "JWTとリフレッシュトークンをCookieから削除します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "ログアウト成功"),
    @ApiResponse(responseCode = "401", description = "認証されていません")
  })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    jwtApplicationService.clearJwtAndRefreshToken(response);
    return ResponseEntity.ok().build();
  }
}
