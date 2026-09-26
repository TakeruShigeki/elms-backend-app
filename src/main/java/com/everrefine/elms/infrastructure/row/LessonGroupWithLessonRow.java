package com.everrefine.elms.infrastructure.row;

import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import com.everrefine.elms.domain.model.lesson.LessonInGroup;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;

/** レッスングループとレッスンをJOINしたセレクト結果の1行。 */
public record LessonGroupWithLessonRow(
    @Id UUID lessonId,
    String lessonTitle,
    BigDecimal lessonOrder,
    @Nullable String lessonContent,
    @Nullable String lessonVideoUrl,
    LocalDateTime lessonCreatedAt,
    LocalDateTime lessonUpdatedAt,
    UUID lessonGroupId,
    UUID courseId,
    String lessonGroupTitle,
    BigDecimal lessonGroupOrder,
    LocalDateTime lessonGroupCreatedAt,
    LocalDateTime lessonGroupUpdatedAt) {

  /**
   * JOIN結果の行一覧を、レッスングループ単位で入れ子にまとめた読み取りモデルへ変換する。
   *
   * <p>行はレッスングループ順・レッスン順で並んでいることを前提とし、その並び順を保持する。 レッスンを持たないグループ（LEFT
   * JOINでレッスンがnullの行）は、空のレッスン一覧を持つグループとして扱う。
   *
   * @param rows JOIN結果の行一覧
   * @return レッスングループごとに配下レッスンをまとめた読み取りモデル一覧
   */
  public static List<LessonGroupWithLessons> toDomainList(List<LessonGroupWithLessonRow> rows) {
    return rows.stream()
        .collect(
            Collectors.groupingBy(
                LessonGroupWithLessonRow::lessonGroupId, LinkedHashMap::new, Collectors.toList()))
        .values()
        .stream()
        .map(LessonGroupWithLessonRow::toLessonGroup)
        .toList();
  }

  /**
   * 同一レッスングループに属する行一覧を、1つのレッスングループの読み取りモデルへ変換する。
   *
   * @param groupRows 同一レッスングループの行一覧
   * @return レッスングループの読み取りモデル
   */
  private static LessonGroupWithLessons toLessonGroup(List<LessonGroupWithLessonRow> groupRows) {
    LessonGroupWithLessonRow head = groupRows.getFirst();
    List<LessonInGroup> lessons =
        groupRows.stream()
            .filter(row -> row.lessonId() != null)
            .map(LessonGroupWithLessonRow::toLessonInGroup)
            .toList();
    return new LessonGroupWithLessons(
        head.lessonGroupId(),
        head.courseId(),
        head.lessonGroupTitle(),
        head.lessonGroupOrder(),
        head.lessonGroupCreatedAt(),
        head.lessonGroupUpdatedAt(),
        lessons);
  }

  /**
   * この行のレッスン部分を、レッスングループ配下のレッスン読み取りモデルへ変換する。
   *
   * @return レッスングループ配下のレッスン読み取りモデル
   */
  private LessonInGroup toLessonInGroup() {
    return new LessonInGroup(
        lessonId,
        lessonTitle,
        lessonOrder,
        lessonContent,
        lessonVideoUrl,
        lessonCreatedAt,
        lessonUpdatedAt);
  }
}
