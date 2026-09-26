package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.news.NewsEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** お知らせのDAOインターフェース。 */
@Repository
public interface NewsDao extends CrudRepository<NewsEntity, UUID> {

  @Query(
      """
      SELECT *
      FROM news
      WHERE id IN (:ids)
      ORDER BY created_at DESC
      """)
  List<NewsEntity> findByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      """
      SELECT id
      FROM news
      WHERE (:newsTitle IS NULL OR :newsTitle = '' OR title LIKE CONCAT('%', :newsTitle, '%'))
        AND (CAST(:createdDateFrom AS DATE) IS NULL OR created_at >= CAST(:createdDateFrom AS DATE))
        AND (CAST(:createdDateTo AS DATE) IS NULL OR created_at < CAST(:createdDateTo AS DATE) + INTERVAL '1 day')
      ORDER BY created_at DESC
      LIMIT :pageSize OFFSET :offset
      """)
  List<UUID> findNewsBySearchConditions(
      @Param("newsTitle") String newsTitle,
      @Param("createdDateFrom") LocalDate createdDateFrom,
      @Param("createdDateTo") LocalDate createdDateTo,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  @Query(
      """
      SELECT COUNT(*)
      FROM news
      WHERE (:newsTitle IS NULL OR :newsTitle = '' OR title LIKE CONCAT('%', :newsTitle, '%'))
        AND (CAST(:createdDateFrom AS DATE) IS NULL OR created_at >= CAST(:createdDateFrom AS DATE))
        AND (CAST(:createdDateTo AS DATE) IS NULL OR created_at < CAST(:createdDateTo AS DATE) + INTERVAL '1 day')
      """)
  int countNewsBySearchConditions(
      @Param("newsTitle") String newsTitle,
      @Param("createdDateFrom") LocalDate createdDateFrom,
      @Param("createdDateTo") LocalDate createdDateTo);
}
