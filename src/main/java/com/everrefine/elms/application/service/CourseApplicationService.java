package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.CourseCreateCommand;
import com.everrefine.elms.application.command.CourseUpdateCommand;
import com.everrefine.elms.application.dto.CourseDto;
import com.everrefine.elms.application.dto.CoursePageDto;
import java.util.UUID;

/** コースアプリケーションサービスのインターフェース。 */
public interface CourseApplicationService {

  /**
   * IDでコースを取得する。
   *
   * @param CourseId コースID
   * @return コースDTO
   */
  CourseDto findCourseById(UUID CourseId);

  /**
   * コース一覧をページング取得する。
   *
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @return コースページDTO
   */
  CoursePageDto findCourses(int pageNum, int pageSize);

  /**
   * コースを作成する。
   *
   * @param courseCreateCommand コース作成Command
   */
  void createCourse(CourseCreateCommand courseCreateCommand);

  /**
   * コースを更新する。
   *
   * @param courseUpdateCommand コース更新Command
   */
  void updateCourse(CourseUpdateCommand courseUpdateCommand);

  /**
   * IDでコースを削除する。
   *
   * @param courseId コースID
   */
  void deleteCourseById(UUID courseId);
}
