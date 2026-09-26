package com.everrefine.elms.application.dto;

import java.util.List;

/** レッスンページDTO。 */
public record LessonPageDto(List<LessonDto> lessonDtos, int pageNum, int pageSize, int totalSize) {

  /**
   * LessonPageDtoを生成する。
   *
   * @param lessonDtos レッスンDTOリスト
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @param totalSize 総データ件数
   * @return レッスンページDTO
   */
  public static LessonPageDto from(
      List<LessonDto> lessonDtos, int pageNum, int pageSize, int totalSize) {
    return new LessonPageDto(lessonDtos, pageNum, pageSize, totalSize);
  }
}
