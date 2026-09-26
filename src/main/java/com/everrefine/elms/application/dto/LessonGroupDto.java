package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.LessonGroup;
import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** レッスングループのDTO。 */
public record LessonGroupDto(
    @Schema(description = "レッスングループID", example = "1") UUID id,
    @Schema(description = "コースID", example = "2") UUID courseId,
    @Schema(description = "レッスングループの表示順", example = "1.0") BigDecimal lessonGroupOrder,
    @Schema(description = "レッスングループ名", example = "第1章: 基礎編") String name,
    @Schema(description = "登録日時", example = "2024-01-01T09:00:00") LocalDateTime createdAt,
    @Schema(description = "更新日時", example = "2024-06-01T10:30:00") LocalDateTime updatedAt,
    @Schema(description = "レッスン一覧") List<LessonDto> lessons) {

  /**
   * LessonGroupエンティティからLessonGroupDtoを生成する（レッスン一覧なし）。
   *
   * @param lessonGroup レッスングループエンティティ
   * @return レッスングループDTO
   */
  public static LessonGroupDto from(LessonGroup lessonGroup) {
    return from(lessonGroup, null);
  }

  /**
   * LessonGroupエンティティとレッスンDTOリストからLessonGroupDtoを生成する。
   *
   * @param lessonGroup レッスングループエンティティ
   * @param lessons レッスンDTOリスト
   * @return レッスングループDTO
   */
  public static LessonGroupDto from(LessonGroup lessonGroup, List<LessonDto> lessons) {
    return new LessonGroupDto(
        lessonGroup.id(),
        lessonGroup.courseId(),
        lessonGroup.lessonGroupOrder().value(),
        lessonGroup.title().value(),
        lessonGroup.createdAt(),
        lessonGroup.updatedAt(),
        lessons);
  }

  /**
   * レッスングループと配下レッスンの読み取りモデルからLessonGroupDtoを生成する。
   *
   * @param group レッスングループと配下レッスンの読み取りモデル
   * @return レッスングループDTO
   */
  public static LessonGroupDto from(LessonGroupWithLessons group) {
    List<LessonDto> lessonDtos =
        group.lessons().stream().map(lesson -> LessonDto.from(group, lesson)).toList();
    return new LessonGroupDto(
        group.id(),
        group.courseId(),
        group.lessonGroupOrder(),
        group.title(),
        group.createdAt(),
        group.updatedAt(),
        lessonDtos);
  }
}
