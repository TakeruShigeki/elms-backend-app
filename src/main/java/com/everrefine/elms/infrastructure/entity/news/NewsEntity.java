package com.everrefine.elms.infrastructure.entity.news;

import com.everrefine.elms.domain.model.news.News;
import com.everrefine.elms.domain.model.news.NewsContent;
import com.everrefine.elms.domain.model.news.NewsTitle;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** お知らせのエンティティ。 */
@Table("news")
public record NewsEntity(
    @Id UUID id, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param news お知らせのドメインモデル
   * @return エンティティ
   */
  public static NewsEntity from(News news) {
    return new NewsEntity(
        news.id(),
        news.title().value(),
        news.content().value(),
        news.createdAt(),
        news.updatedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return お知らせのドメインモデル
   */
  public News toDomain() {
    return new News(id, new NewsTitle(title), new NewsContent(content), createdAt, updatedAt);
  }
}
