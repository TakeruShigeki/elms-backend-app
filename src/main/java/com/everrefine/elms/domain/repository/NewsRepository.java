package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.news.News;
import com.everrefine.elms.domain.model.news.NewsSearchCondition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** お知らせのリポジトリインターフェース。 */
public interface NewsRepository {

  /**
   * 複数のIDでお知らせ一覧を取得する。
   *
   * @param newsIds お知らせIDのリスト
   * @return お知らせ一覧
   */
  List<News> findNewsByIds(List<UUID> newsIds);

  /**
   * 検索条件に合致するお知らせの件数を取得する。
   *
   * @param newsSearchCondition 検索条件
   * @return お知らせの件数
   */
  int countNews(NewsSearchCondition newsSearchCondition);

  /**
   * お知らせを作成する。
   *
   * @param news 作成するお知らせ
   */
  void createNews(News news);

  /**
   * IDでお知らせを削除する。
   *
   * @param id お知らせID
   */
  void deleteNewsById(UUID id);

  /**
   * お知らせを更新する。
   *
   * @param news 更新するお知らせ
   */
  void updateNews(News news);

  /**
   * 検索条件に合致するお知らせIDのリストを取得する。
   *
   * @param newsSearchCondition 検索条件
   * @return お知らせIDのリスト
   */
  List<UUID> findNewsIdsBySearchConditions(NewsSearchCondition newsSearchCondition);

  /**
   * IDでお知らせを取得する。
   *
   * @param id お知らせID
   * @return お知らせ（存在しない場合は空）
   */
  Optional<News> findNewsById(UUID id);
}
