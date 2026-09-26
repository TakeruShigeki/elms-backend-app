package com.everrefine.elms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** ユーザーページDTO。 */
public record UserPageDto(
    @Schema(description = "ユーザー一覧") List<UserDto> userDtos,
    @Schema(description = "現在のページ番号", example = "1") int pageNum,
    @Schema(description = "1ページ当たりの件数", example = "10") int pageSize,
    @Schema(description = "総データ件数", example = "42") int totalSize) {

  /**
   * UserPageDtoを生成する。
   *
   * @param userDtos ユーザーDTOリスト
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @param totalSize 総データ件数
   * @return ユーザーページDTO
   */
  public static UserPageDto from(List<UserDto> userDtos, int pageNum, int pageSize, int totalSize) {
    return new UserPageDto(userDtos, pageNum, pageSize, totalSize);
  }
}
