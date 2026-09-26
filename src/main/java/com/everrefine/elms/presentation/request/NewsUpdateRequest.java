package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.NewsUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/** お知らせ更新リクエスト。 */
public record NewsUpdateRequest(
    @Schema(description = "お知らせID", example = "1") String id,
    @Schema(description = "お知らせタイトル", example = "システムメンテナンスのお知らせ") @NotBlank @Size(max = 255)
        String title,
    @Schema(description = "お知らせ本文", example = "4月1日にシステムメンテナンスを行います。") @NotBlank String content) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @param newsId お知らせID
   * @return お知らせ更新Command
   */
  public NewsUpdateCommand toCommand(UUID newsId) {
    return new NewsUpdateCommand(newsId, title, content, LocalDateTime.now());
  }
}
