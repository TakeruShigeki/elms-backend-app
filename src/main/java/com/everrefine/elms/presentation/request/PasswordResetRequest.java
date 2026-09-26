package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.PasswordResetRequestCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** パスワードリセットリクエスト。 */
public record PasswordResetRequest(
    @Schema(description = "パスワードリセット対象のメールアドレス", example = "yamada@example.com")
        @Size(max = 255)
        @NotBlank
        String emailAddress) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @return パスワードリセットリクエストCommand
   */
  public PasswordResetRequestCommand toCommand() {
    return new PasswordResetRequestCommand(emailAddress);
  }
}
