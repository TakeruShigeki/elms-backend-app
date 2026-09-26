package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.news.News;
import com.everrefine.elms.domain.model.news.NewsSearchCondition;
import com.everrefine.elms.domain.repository.NewsRepository;
import com.everrefine.elms.infrastructure.dao.NewsDao;
import com.everrefine.elms.infrastructure.entity.news.NewsEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

/** {@link NewsRepository} の実装。 */
@Repository
@AllArgsConstructor
public class NewsRepositoryImpl implements NewsRepository {

  private final NewsDao newsDao;

  @Override
  public List<News> findNewsByIds(List<UUID> newsIds) {
    if (newsIds.isEmpty()) {
      return Collections.emptyList();
    }
    return newsDao.findByIdIn(newsIds).stream().map(NewsEntity::toDomain).toList();
  }

  @Override
  public int countNews(NewsSearchCondition newsSearchCondition) {
    return newsDao.countNewsBySearchConditions(
        newsSearchCondition.title(),
        newsSearchCondition.createdDateFrom() == null
            ? null
            : newsSearchCondition.createdDateFrom(),
        newsSearchCondition.createdDateTo() == null ? null : newsSearchCondition.createdDateTo());
  }

  @Override
  public void createNews(News news) {
    newsDao.save(NewsEntity.from(news));
  }

  @Override
  public void deleteNewsById(UUID id) {
    newsDao.deleteById(id);
  }

  @Override
  public void updateNews(News news) {
    newsDao.save(NewsEntity.from(news));
  }

  @Override
  public List<UUID> findNewsIdsBySearchConditions(NewsSearchCondition newsSearchCondition) {
    return newsDao.findNewsBySearchConditions(
        newsSearchCondition.title(),
        newsSearchCondition.createdDateFrom() == null
            ? null
            : newsSearchCondition.createdDateFrom(),
        newsSearchCondition.createdDateTo() == null ? null : newsSearchCondition.createdDateTo(),
        newsSearchCondition.pagerForRequest().pageSize(),
        newsSearchCondition.pagerForRequest().getOffset());
  }

  @Override
  public Optional<News> findNewsById(UUID id) {
    return newsDao.findById(id).map(NewsEntity::toDomain);
  }
}
