package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.news.News;
import java.util.UUID;

/** お知らせ作成用のコマンド。 */
public record NewsCreateCommand(UUID id, String title, String content) {

  /**
   * Newsエンティティに変換する。
   *
   * @return お知らせエンティティ
   */
  public News toNews() {
    return News.create(title, content);
  }
}
