package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.UserSearchCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** ユーザー検索リクエスト。 */
public record UserSearchRequest(
    @Schema(description = "ページ番号（1始まり）", example = "1") @Positive Integer pageNum,
    @Schema(description = "1ページ当たりの件数", example = "10") @Positive Integer pageSize,
    @Schema(description = "ユーザーID（部分一致）", example = "550e8400-e29b-41d4-a716-446655440000")
        @Pattern(regexp = "[0-9a-fA-F\\-]*", message = "ユーザーIDはUUIDの形式（16進数とハイフン）で指定してください")
        String userId,
    @Schema(description = "ユーザーロール（GENERAL / ADMIN）", example = "GENERAL") @Size(max = 10)
        String userRole,
    @Schema(description = "本名（部分一致）", example = "山田") @Size(max = 50) String realName,
    @Schema(description = "ユーザー名（部分一致）", example = "yamada") @Size(max = 50) String userName,
    @Schema(description = "メールアドレス（部分一致）", example = "yamada@example.com") @Size(max = 255)
        String emailAddress,
    @Schema(description = "登録日From", example = "2024-01-01") LocalDate createdDateFrom,
    @Schema(description = "登録日To", example = "2024-12-31") LocalDate createdDateTo) {

  /**
   * Commandオブジェクトに変換する。ページ番号・件数が未指定の場合はデフォルト値（1 / 10）を適用する。
   *
   * @return ユーザー検索Command
   */
  public UserSearchCommand toCommand() {
    return new UserSearchCommand(
        pageNum == null ? 1 : pageNum,
        pageSize == null ? 10 : pageSize,
        userId,
        userRole,
        realName,
        userName,
        emailAddress,
        createdDateFrom,
        createdDateTo);
  }
}
