package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.lesson.LessonEntity;
import com.everrefine.elms.infrastructure.row.LessonWithCourseAndLessonGroupRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** レッスンのDAOインターフェース。 */
public interface LessonDao extends CrudRepository<LessonEntity, UUID> {

  @Query(
      """
          SELECT * FROM lessons WHERE
          (CAST(:courseId AS UUID) IS NULL OR course_id = CAST(:courseId AS UUID)) AND
          (CAST(:lessonGroupId AS UUID) IS NULL OR lesson_group_id = CAST(:lessonGroupId AS UUID)) AND
          (:title IS NULL OR title LIKE CONCAT('%', :title, '%')) AND
          (CAST(:createdDateFrom AS DATE) IS NULL OR created_at >= CAST(:createdDateFrom AS DATE)) AND
          (CAST(:createdDateTo AS DATE) IS NULL OR created_at < CAST(:createdDateTo AS DATE) + INTERVAL '1 day')
          ORDER BY lesson_order ASC
          LIMIT :limit OFFSET :offset
          """)
  List<LessonEntity> findLessons(
      @Param("courseId") UUID courseId,
      @Param("lessonGroupId") UUID lessonGroupId,
      @Param("title") String title,
      @Param("createdDateFrom") LocalDate createdDateFrom,
      @Param("createdDateTo") LocalDate createdDateTo,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Query(
      """
          SELECT COUNT(*) FROM lessons WHERE
          (CAST(:courseId AS UUID) IS NULL OR course_id = CAST(:courseId AS UUID)) AND
          (CAST(:lessonGroupId AS UUID) IS NULL OR lesson_group_id = CAST(:lessonGroupId AS UUID)) AND
          (:title IS NULL OR title LIKE CONCAT('%', :title, '%')) AND
          (CAST(:createdDateFrom AS DATE) IS NULL OR created_at >= CAST(:createdDateFrom AS DATE)) AND
          (CAST(:createdDateTo AS DATE) IS NULL OR created_at < CAST(:createdDateTo AS DATE) + INTERVAL '1 day')
          """)
  int countLessons(
      @Param("courseId") UUID courseId,
      @Param("lessonGroupId") UUID lessonGroupId,
      @Param("title") String title,
      @Param("createdDateFrom") LocalDate createdDateFrom,
      @Param("createdDateTo") LocalDate createdDateTo);

  @Query(
      """
          SELECT MAX(lesson_order)
          FROM lessons
          WHERE lesson_group_id = :lessonGroupId
          """)
  Optional<BigDecimal> findMaxLessonOrderByLessonGroupId(
      @Param("lessonGroupId") UUID lessonGroupId);

  List<LessonEntity> findByIdIn(@Param("lessonIds") List<UUID> lessonIds);

  @Query(
      """
          SELECT *
          FROM lessons
          WHERE lesson_group_id = :lessonGroupId
          ORDER BY lesson_order ASC
          """)
  List<LessonEntity> findLessonsByLessonGroupId(@Param("lessonGroupId") UUID lessonGroupId);

  @Modifying
  @Query(
      """
          DELETE FROM lessons
          WHERE course_id = :courseId
          """)
  void deleteByCourseId(@Param("courseId") UUID courseId);

  @Query(
      """
      SELECT COUNT(*)
      FROM lessons
      """)
  int countAllLessons();

  @Query(
      """
          SELECT video_url
          FROM lessons
          WHERE video_url LIKE CONCAT(:prefix, '%')
          """)
  List<String> findByVideoUrlStartingWith(String prefix);

  @Query(
      """
         SELECT
          c.id AS course_id,
          c.title AS course_title,
          lg.id AS lesson_group_id,
          lg.title AS lesson_group_title,
          l.id AS lesson_id,
          l.title AS lesson_title,
          l.video_url AS lesson_video_url
         FROM courses c
          INNER JOIN lesson_groups lg
          ON lg.course_id = c.id
          INNER JOIN lessons l
          ON l.lesson_group_id = lg.id
         ORDER BY c.course_order ASC, lg.lesson_group_order ASC, l.lesson_order ASC
         """)
  List<LessonWithCourseAndLessonGroupRow> findByAllLessons();
}
