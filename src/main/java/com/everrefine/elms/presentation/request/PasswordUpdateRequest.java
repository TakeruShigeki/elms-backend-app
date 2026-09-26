package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.PasswordUpdateCommand;
import jakarta.validation.constraints.NotBlank;

/** パスワード変更リクエスト。 */
public record PasswordUpdateRequest(
    @NotBlank String currentPassword, @NotBlank String newPassword) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @return パスワード変更Command
   */
  public PasswordUpdateCommand toCommand() {
    return new PasswordUpdateCommand(currentPassword, newPassword);
  }
}
