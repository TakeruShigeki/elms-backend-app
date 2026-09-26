package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.LoginHistoryCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** ログインリクエスト。 */
public record LoginRequest(
    @NotBlank @Schema(description = "メールアドレス", example = "user@example.com") String emailAddress,
    @NotBlank @Schema(description = "パスワード", example = "password123") String password,
    @Schema(description = "ログイン状態を保持するか（true: リフレッシュトークンの有効期限を延長）", example = "false")
        boolean rememberMe) {

  /**
   * ログイン履歴作成Commandに変換する。
   *
   * @return ログイン履歴作成Command
   */
  public LoginHistoryCreateCommand toLoginHistoryCreateCommand() {
    return new LoginHistoryCreateCommand(emailAddress);
  }
}
