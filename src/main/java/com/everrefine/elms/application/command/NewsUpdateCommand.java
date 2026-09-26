package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.news.News;
import java.time.LocalDateTime;
import java.util.UUID;

/** お知らせ更新用のコマンド。 */
public record NewsUpdateCommand(UUID id, String title, String content, LocalDateTime updatedAt) {

  /**
   * Newsエンティティに変換する。
   *
   * @param news 更新対象のお知らせ
   * @return 更新後のお知らせエンティティ
   */
  public News toNews(News news) {
    return news.update(title, content);
  }
}
