package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.tag.LessonTagEntity;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** レッスンとタグの紐付けのDAOインターフェース。 */
@Repository
public interface LessonTagDao extends CrudRepository<LessonTagEntity, UUID> {

  /**
   * レッスンIDに紐づくタグの紐付けをすべて削除する。
   *
   * <p>本APIは部分更新を行わないため、更新のたびに既存の紐付けを全削除してから入れ直す（洗い替え）。
   *
   * @param lessonId レッスンID
   */
  @Modifying
  @Query(
      """
          DELETE FROM lesson_tags
          WHERE lesson_id = :lessonId
          """)
  void deleteByLessonId(@Param("lessonId") UUID lessonId);

  /**
   * レッスンとタグの紐付けを登録する。
   *
   * @param lessonId レッスンID
   * @param tagId タグID
   */
  @Modifying
  @Query(
      """
          INSERT INTO lesson_tags(lesson_id, tag_id)
          VALUES(:lessonId, :tagId)
          """)
  void create(@Param("lessonId") UUID lessonId, @Param("tagId") UUID tagId);
}
