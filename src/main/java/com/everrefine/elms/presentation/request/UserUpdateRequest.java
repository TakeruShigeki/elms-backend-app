package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.UserUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/** ユーザー更新リクエスト。 */
public record UserUpdateRequest(
    @Schema(description = "ユーザーID", example = "1") String userId,
    @Schema(description = "本名", example = "山田太郎") @NotBlank @Size(max = 50) String realName,
    @Schema(description = "ユーザー名", example = "yamada_taro") @NotBlank @Size(max = 50)
        String userName,
    @Schema(description = "メールアドレス", example = "yamada@example.com") @NotBlank @Size(max = 255)
        String emailAddress,
    @Schema(description = "サムネイルURL", example = "https://example.com/thumbnail.png")
        String thumbnailUrl) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @param userId ユーザーID
   * @return ユーザー更新Command
   */
  public UserUpdateCommand toCommand(UUID userId) {
    return new UserUpdateCommand(
        userId, realName, userName, emailAddress, thumbnailUrl, LocalDateTime.now());
  }
}
