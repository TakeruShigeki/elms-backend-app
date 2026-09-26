package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import com.everrefine.elms.domain.model.lesson.LessonSearchCriteria;
import com.everrefine.elms.domain.model.lesson.LessonWithCourseAndLessonGroup;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** レッスンのリポジトリインターフェース。 */
public interface LessonRepository {

  /**
   * IDでレッスンを取得する。
   *
   * @param lessonId レッスンID
   * @return レッスン（存在しない場合は空）
   */
  Optional<Lesson> findById(UUID lessonId);

  /**
   * 複数のIDでレッスン一覧を取得する。
   *
   * @param lessonIds レッスンIDのリスト
   * @return レッスン一覧
   */
  List<Lesson> findByIdIn(List<UUID> lessonIds);

  /**
   * 検索条件に合致するレッスン一覧を取得する。
   *
   * @param criteria レッスン検索条件
   * @return レッスン一覧
   */
  List<Lesson> findLessons(LessonSearchCriteria criteria);

  /**
   * 検索条件に合致するレッスンの件数を取得する。
   *
   * @param criteria レッスン検索条件
   * @return レッスンの件数
   */
  int countLessons(LessonSearchCriteria criteria);

  /**
   * レッスングループIDに紐づくレッスン一覧を取得する。
   *
   * @param lessonGroupId レッスングループID
   * @return レッスン一覧
   */
  List<Lesson> findLessonsByLessonGroupId(UUID lessonGroupId);

  /**
   * コースIDに紐づくレッスングループごとのレッスン一覧を取得する。
   *
   * @param courseId コースID
   * @return レッスングループとレッスンの一覧
   */
  List<LessonGroupWithLessons> findLessonsGroupedByLessonGroup(UUID courseId);

  /**
   * レッスンを作成する。
   *
   * @param lesson 作成するレッスン
   * @return 作成されたレッスン
   */
  Lesson createLesson(Lesson lesson);

  /**
   * 複数のレッスンを作成する。
   *
   * @param lessons 作成するレッスンのリスト
   */
  void createLessons(List<Lesson> lessons);

  /**
   * レッスンを更新する。
   *
   * @param lesson 更新するレッスン
   * @return 更新されたレッスン
   */
  Lesson updateLesson(Lesson lesson);

  /**
   * レッスングループ内の最大lesson_orderを取得する。
   *
   * @param lessonGroupId レッスングループID
   * @return 最大lesson_order（レッスンが存在しない場合は空）
   */
  Optional<BigDecimal> findMaxLessonOrderByLessonGroupId(UUID lessonGroupId);

  /**
   * IDでレッスンを削除する。
   *
   * @param lessonId レッスンID
   */
  void deleteLessonById(UUID lessonId);

  /**
   * コースIDに紐づくレッスンを削除する。
   *
   * @param courseId コースID
   */
  void deleteLessonsByCourseId(UUID courseId);

  /**
   * レッスンの総件数を取得する。
   *
   * @return レッスンの総件数
   */
  int countAllLessons();

  /**
   * 指定プレフィックスで始まる動画URLを取得する。
   *
   * @param prefix URLのプレフィックス
   * @return 動画URLのリスト
   */
  List<String> findByVideoUrlStartingWith(String prefix);

  /**
   * コース・レッスングループ情報を含む全レッスン一覧を取得する。
   *
   * @return 全レッスン一覧
   */
  List<LessonWithCourseAndLessonGroup> findAllLessons();
}
