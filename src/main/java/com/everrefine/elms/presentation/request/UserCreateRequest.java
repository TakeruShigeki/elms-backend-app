package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.UserCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ユーザー作成リクエスト。 */
public record UserCreateRequest(
    @Schema(description = "本名", example = "山田太郎") @NotBlank @Size(max = 50) String realName,
    @Schema(description = "ユーザー名", example = "yamada_taro") @NotBlank @Size(max = 50)
        String userName,
    @Schema(description = "メールアドレス", example = "yamada@example.com") @NotBlank @Size(max = 255)
        String emailAddress,
    @Schema(description = "パスワード", example = "password123") @NotBlank @Size(max = 255)
        String password,
    @Schema(description = "確認用パスワード（passwordと一致する必要があります）", example = "password123")
        @NotBlank
        @Size(max = 255)
        String confirmPassword,
    @Schema(description = "サムネイルURL", example = "https://example.com/thumbnail.png")
        String thumbnailUrl,
    @Schema(description = "ユーザーロール（GENERAL: 一般, ADMIN: 管理者）", example = "GENERAL") @NotBlank
        String userRole) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @return ユーザー作成Command
   */
  public UserCreateCommand toCommand() {
    return new UserCreateCommand(
        null, realName, userName, emailAddress, password, confirmPassword, thumbnailUrl, userRole);
  }
}
