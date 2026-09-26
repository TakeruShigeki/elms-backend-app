package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.NewsSearchCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** お知らせ検索リクエスト。 */
public record NewsSearchRequest(
    @Schema(description = "ページ番号（1始まり）", example = "1") @Positive Integer pageNum,
    @Schema(description = "1ページ当たりの件数", example = "10") @Positive Integer pageSize,
    @Schema(description = "タイトル（部分一致）", example = "メンテナンス") @Size(max = 255) String title,
    @Schema(description = "登録日From", example = "2024-01-01") LocalDate createdDateFrom,
    @Schema(description = "登録日To", example = "2024-12-31") LocalDate createdDateTo) {

  /**
   * Commandオブジェクトに変換する。ページ番号・件数が未指定の場合はデフォルト値（1 / 10）を適用する。
   *
   * @return お知らせ検索Command
   */
  public NewsSearchCommand toCommand() {
    return new NewsSearchCommand(
        pageNum == null ? 1 : pageNum,
        pageSize == null ? 10 : pageSize,
        title,
        createdDateFrom,
        createdDateTo);
  }
}
