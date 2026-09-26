package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.service.JwtApplicationService;
import com.everrefine.elms.application.service.PasswordResetApplicationService;
import com.everrefine.elms.presentation.request.PasswordResetConfirmRequest;
import com.everrefine.elms.presentation.request.PasswordResetRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** パスワードリセットに関するコントローラー。 */
@Tag(name = "パスワードリセット")
@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

  private final PasswordResetApplicationService passwordResetApplicationService;
  private final AuthenticationManager authenticationManager;
  private final JwtApplicationService jwtApplicationService;

  /**
   * 指定したメールアドレス宛にパスワードリセット用メールを送信する。
   *
   * @param request パスワードリセットリクエスト（メールアドレス）
   */
  @Operation(summary = "パスワードリセットリクエスト", description = "指定したメールアドレス宛にパスワードリセット用メールを送信します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "メール送信成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー")
  })
  @SecurityRequirement(name = "")
  @PostMapping("/request")
  public ResponseEntity<Void> requestPasswordReset(
      @RequestBody @Valid PasswordResetRequest request) {
    passwordResetApplicationService.requestPasswordReset(request.toCommand());
    return ResponseEntity.ok().build();
  }

  /**
   * リセットトークンと新しいパスワードでパスワードを変更し、自動ログインする。
   *
   * @param request パスワードリセット確定リクエスト（トークン・新パスワード）
   * @param response HTTPレスポンス（自動ログイン用Cookie操作用）
   */
  @Operation(summary = "パスワードリセット確定", description = "リセットトークンと新しいパスワードでパスワードを変更し、自動ログインします")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "パスワードリセット成功（JWTをCookieにセット）"),
    @ApiResponse(responseCode = "400", description = "トークンが無効・期限切れ・使用済み、またはユーザーが見つかりません")
  })
  @SecurityRequirement(name = "")
  @PostMapping("/confirm")
  public ResponseEntity<Void> confirmPasswordReset(
      @RequestBody @Valid PasswordResetConfirmRequest request, HttpServletResponse response) {
    try {
      String emailAddress =
          passwordResetApplicationService.confirmPasswordReset(request.toCommand());

      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(emailAddress, request.newPassword()));

      String jwtToken = jwtApplicationService.generateJwtToken(authentication.getName());
      // パスワードリセット後の自動ログインはrememberMe=false（セッションCookie）とする
      String refreshToken =
          jwtApplicationService.generateRefreshToken(authentication.getName(), false);
      jwtApplicationService.setJwtTokenToResponseCookie(response, jwtToken);
      jwtApplicationService.setRefreshTokenToResponseCookie(response, refreshToken, false);

      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}
