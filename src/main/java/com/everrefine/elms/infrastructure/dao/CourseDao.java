package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.course.CourseEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** コースのDAOインターフェース。 */
@Repository
public interface CourseDao extends CrudRepository<CourseEntity, UUID> {

  @Query(
      """
            SELECT *
            FROM courses
            ORDER BY course_order ASC
            LIMIT :pageSize
            OFFSET :offset
          """)
  List<CourseEntity> findCoursesWithPagination(
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  @Query(
      """
        SELECT COUNT(*)
        FROM courses
      """)
  int countAllCourses();

  Optional<CourseEntity> findTop1ByOrderByCourseOrderDesc();

  @Query(
      """
          SELECT *
          FROM courses
          WHERE course_order = :courseOrder
          """)
  Optional<CourseEntity> findByCourseOrder(@Param("courseOrder") BigDecimal courseOrder);

  @Query(
      """
          SELECT thumbnail_url
          FROM courses
          WHERE thumbnail_url LIKE CONCAT(:prefix, '%')
          """)
  List<String> findByThumbnailUrlStartingWith(String prefix);
}
