package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.news.News;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

/** お知らせDTO。 */
public record NewsDto(
    @Schema(description = "お知らせID", example = "1") UUID id,
    @Schema(description = "お知らせタイトル", example = "システムメンテナンスのお知らせ") String title,
    @Schema(description = "お知らせ本文", example = "4月1日にシステムメンテナンスを行います。") String content,
    @Schema(description = "登録日", example = "2024-01-01") LocalDate createdAt,
    @Schema(description = "更新日", example = "2024-06-01") LocalDate updatedAt) {

  /**
   * NewsエンティティからNewsDtoを生成する。
   *
   * @param news お知らせエンティティ
   * @return お知らせDTO
   */
  public static NewsDto from(News news) {
    return new NewsDto(
        news.id(),
        news.title().value(),
        news.content().value(),
        news.createdAt().toLocalDate(),
        news.updatedAt().toLocalDate());
  }
}
