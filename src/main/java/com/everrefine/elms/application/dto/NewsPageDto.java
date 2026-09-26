package com.everrefine.elms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** お知らせページDTO。 */
public record NewsPageDto(
    @Schema(description = "お知らせ一覧") List<NewsDto> newsDtos,
    @Schema(description = "現在のページ番号", example = "1") int pageNum,
    @Schema(description = "1ページ当たりの件数", example = "10") int pageSize,
    @Schema(description = "総データ件数", example = "20") int totalSize) {

  /**
   * NewsPageDtoを生成する。
   *
   * @param newsDtos お知らせDTOリスト
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @param totalSize 総データ件数
   * @return お知らせページDTO
   */
  public static NewsPageDto from(List<NewsDto> newsDtos, int pageNum, int pageSize, int totalSize) {
    return new NewsPageDto(newsDtos, pageNum, pageSize, totalSize);
  }
}
