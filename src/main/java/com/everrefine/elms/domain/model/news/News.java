package com.everrefine.elms.domain.model.news;

import java.time.LocalDateTime;
import java.util.UUID;

/** お知らせのドメインモデル。 */
public record News(
    UUID id,
    NewsTitle title,
    NewsContent content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * 新規作成用のお知らせを作成する。
   *
   * @param title タイトル
   * @param content 内容
   * @return 新規作成用のお知らせ
   */
  public static News create(String title, String content) {
    LocalDateTime now = LocalDateTime.now();
    return new News(null, new NewsTitle(title), new NewsContent(content), now, now);
  }

  /**
   * 更新用のお知らせを作成する。
   *
   * @param title タイトル
   * @param content 内容
   * @return 更新用のお知らせ
   */
  public News update(String title, String content) {
    return new News(
        this.id,
        title == null ? this.title : new NewsTitle(title),
        content == null ? this.content : new NewsContent(content),
        this.createdAt,
        LocalDateTime.now());
  }
}
