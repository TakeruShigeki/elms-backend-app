package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.LessonCreateCommand;
import com.everrefine.elms.application.command.LessonImportCommand;
import com.everrefine.elms.application.command.LessonOrderUpdateCommand;
import com.everrefine.elms.application.command.LessonSearchCommand;
import com.everrefine.elms.application.command.LessonUpdateCommand;
import com.everrefine.elms.application.dto.CourseLessonsDto;
import com.everrefine.elms.application.dto.LessonDto;
import com.everrefine.elms.application.dto.LessonImportResponseDto;
import com.everrefine.elms.application.dto.LessonPageDto;
import java.util.UUID;
import org.springframework.core.io.Resource;

/** レッスンアプリケーションサービスのインターフェース。 */
public interface LessonApplicationService {

  /**
   * IDでレッスンを取得する。
   *
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonId レッスンID
   * @return レッスンDTO
   */
  LessonDto findLessonById(UUID courseId, UUID lessonGroupId, UUID lessonId);

  /**
   * レッスン一覧をページング取得する。
   *
   * @param lessonSearchCommand レッスン検索Command
   * @return レッスンページDTO
   */
  LessonPageDto findLessons(LessonSearchCommand lessonSearchCommand);

  /**
   * コースIDに紐づくレッスンをレッスングループごとにまとめて取得する。
   *
   * @param courseId コースID
   * @return コースとレッスン一覧DTO
   */
  CourseLessonsDto findLessonsGroupedByLessonGroup(UUID courseId);

  /**
   * レッスンを作成する。
   *
   * @param lessonCreateCommand レッスン作成Command
   * @return 作成したレッスンDTO
   */
  LessonDto createLesson(LessonCreateCommand lessonCreateCommand);

  /**
   * レッスンを更新する。
   *
   * @param lessonUpdateCommand レッスン更新Command
   * @return 更新したレッスンDTO
   */
  LessonDto updateLesson(LessonUpdateCommand lessonUpdateCommand);

  /**
   * IDでレッスンを削除する。
   *
   * @param lessonId レッスンID
   */
  void deleteLessonById(UUID lessonId);

  /**
   * レッスン順序を更新する。
   *
   * @param lessonOrderUpdateCommand レッスン順序更新Command
   * @return 更新したレッスンDTO
   */
  LessonDto updateLessonOrder(LessonOrderUpdateCommand lessonOrderUpdateCommand);

  /**
   * 全レッスンをCSV形式でエクスポートする。
   *
   * @return CSVリソース
   */
  Resource exportAllLessonsCsv();

  /**
   * CSVファイルをアップロードして指定コースのレッスン構成を一括更新する。
   *
   * @param lessonImportCommand レッスン取込用Command
   * @return 取込したレッスングループ件数とレッスン件数
   */
  LessonImportResponseDto importLessonsCsv(LessonImportCommand lessonImportCommand);
}
