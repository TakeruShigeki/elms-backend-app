package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** パスワードリセット確定リクエスト。 */
public record PasswordResetConfirmRequest(
    @Schema(description = "パスワードリセットトークン（メールに記載されたURL内のトークン）", example = "abc123def456")
        @NotBlank
        @Size(max = 255)
        String token,
    @Schema(description = "新しいパスワード", example = "newPassword123") @NotBlank @Size(max = 255)
        String newPassword) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @return パスワードリセット確定Command
   */
  public PasswordResetConfirmCommand toCommand() {
    return new PasswordResetConfirmCommand(token, newPassword);
  }
}
