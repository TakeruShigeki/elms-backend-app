package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.UserLessonEntity;
import com.everrefine.elms.infrastructure.row.CompletedLessonCountRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** ユーザーレッスンのDAOインターフェース。 */
@Repository
public interface UserLessonDao extends CrudRepository<UserLessonEntity, UUID> {

  Optional<UserLessonEntity> findByUserIdAndLessonId(UUID userId, UUID lessonId);

  @Query(
      """
          SELECT lesson_id
          FROM user_lessons
          WHERE user_id = :userId
            AND lesson_id IN (:lessonIds)
      """)
  Set<UUID> findLessonIdByUserIdAndLessonIdIn(
      @Param("userId") UUID userId, @Param("lessonIds") Set<UUID> lessonIds);

  @Modifying
  @Query(
      """
          INSERT INTO user_lessons(user_id, lesson_id, created_at, updated_at)
          VALUES(:userId, :lessonId, :createdAt, :updatedAt)
          """)
  void create(
      @Param("userId") UUID userId,
      @Param("lessonId") UUID lessonId,
      @Param("createdAt") LocalDateTime createdAt,
      @Param("updatedAt") LocalDateTime updatedAt);

  @Modifying
  @Query(
      """
          UPDATE user_lessons
          SET updated_at = :updatedAt
          WHERE user_id = :userId AND lesson_id = :lessonId
          """)
  void update(
      @Param("updatedAt") LocalDateTime updatedAt,
      @Param("userId") UUID userId,
      @Param("lessonId") UUID lessonId);

  void deleteByUserIdAndLessonId(UUID userId, UUID lessonId);

  @Query(
      """
          SELECT COUNT(*)
          FROM user_lessons
          WHERE user_id = :userId
          """)
  int countAllByUserId(@Param("userId") UUID userId);

  @Query(
      """
          SELECT user_id, COUNT(*) as completed_count
          FROM user_lessons
          WHERE user_id IN (:userIds)
          GROUP BY user_id
          """)
  List<CompletedLessonCountRow> countByUserIds(@Param("userIds") List<UUID> userIds);

  @Query(
      """
          SELECT COUNT(*)
          FROM user_lessons ul
          INNER JOIN lessons l ON ul.lesson_id = l.id
          WHERE ul.user_id = :userId
          AND l.course_id = :courseId
          """)
  int countCompletedLessonsByUserIdAndCourseId(
      @Param("userId") UUID userId, @Param("courseId") UUID courseId);
}
