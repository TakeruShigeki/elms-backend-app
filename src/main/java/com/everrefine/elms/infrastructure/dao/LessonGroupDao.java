package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.lesson.LessonGroupEntity;
import com.everrefine.elms.infrastructure.row.LessonGroupWithLessonRow;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** レッスングループのDAOインターフェース。 */
@Repository
public interface LessonGroupDao extends CrudRepository<LessonGroupEntity, UUID> {

  @Query(
      """
      SELECT
        l.id as lesson_id,
        l.title as lesson_title,
        l.lesson_order,
        l.content as lesson_content,
        l.video_url as lesson_video_url,
        l.created_at as lesson_created_at,
        l.updated_at as lesson_updated_at,
        lg.id as lesson_group_id,
        lg.course_id as course_id,
        lg.title as lesson_group_title,
        lg.lesson_group_order,
        lg.created_at as lesson_group_created_at,
        lg.updated_at as lesson_group_updated_at
      FROM lesson_groups lg
      LEFT JOIN lessons l ON lg.id = l.lesson_group_id
      WHERE lg.course_id = :courseId
      ORDER BY lg.lesson_group_order ASC, l.lesson_order ASC
      """)
  List<LessonGroupWithLessonRow> findLessonGroupsByCourseId(@Param("courseId") UUID courseId);

  @Query(
      """
      SELECT MAX(lesson_group_order)
      FROM lesson_groups
      WHERE course_id = :courseId
      """)
  Optional<BigDecimal> findMaxLessonGroupOrderByCourseId(@Param("courseId") UUID courseId);

  @Modifying
  @Query(
      """
      DELETE FROM lesson_groups
      WHERE course_id = :courseId
      """)
  void deleteByCourseId(@Param("courseId") UUID courseId);
}
