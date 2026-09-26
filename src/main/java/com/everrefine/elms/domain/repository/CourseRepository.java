package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.PagerForRequest;
import com.everrefine.elms.domain.model.course.Course;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** コースのリポジトリインターフェース。 */
public interface CourseRepository {

  /**
   * コースを更新する。
   *
   * @param course 更新するコース
   */
  void updateCourse(Course course);

  /**
   * コースを作成する。
   *
   * @param course 作成するコース
   */
  void createCourse(Course course);

  /**
   * IDでコースを削除する。
   *
   * @param id コースID
   */
  void deleteCourseById(UUID id);

  /**
   * IDでコースを取得する。
   *
   * @param id コースID
   * @return コース（存在しない場合は空）
   */
  Optional<Course> findCourseById(UUID id);

  /**
   * 表示順でコースを取得する。
   *
   * @param courseOrder コースの表示順
   * @return コース（存在しない場合は空）
   */
  Optional<Course> findCourseByCourseOrder(Order courseOrder);

  /**
   * ページングしてコース一覧を取得する。
   *
   * @param pagerForRequest ページング情報
   * @return コース一覧
   */
  List<Course> findCourses(PagerForRequest pagerForRequest);

  /**
   * コースの総件数を取得する。
   *
   * @return コースの総件数
   */
  int countCourses();

  /**
   * コースの並び順を発番する。
   *
   * @return 発番されたコースの並び順
   */
  Order issueCourseOrder();

  /**
   * 指定プレフィックスで始まるサムネイルURLを取得する。
   *
   * @param prefix URLのプレフィックス
   * @return サムネイルURLのリスト
   */
  List<String> findByThumbnailUrlStartingWith(String prefix);
}
