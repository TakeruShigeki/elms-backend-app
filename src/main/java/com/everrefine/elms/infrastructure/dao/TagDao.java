package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.tag.TagEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** タグのDAOインターフェース。 */
@Repository
public interface TagDao extends CrudRepository<TagEntity, UUID> {

  /**
   * タグが存在しない場合のみ登録する。
   *
   * <p>タグ名は全レッスンで共有するマスタなので、他のレッスンが既に使っているタグ名はそのまま使い回す。{@code SELECT} してから {@code INSERT}
   * する実装は同時リクエストで二重登録されうるが、{@code ON CONFLICT} ならDBの一意制約で解決される。
   *
   * @param name タグ名
   */
  @Modifying
  @Query(
      """
          INSERT INTO tags(name)
          VALUES(:name)
          ON CONFLICT (name) DO NOTHING
          """)
  void createIfAbsent(@Param("name") String name);

  /**
   * タグ名でタグ一覧を取得する。
   *
   * <p>{@code lesson_tags} に登録するのはタグ名ではなく {@code tag_id} のため、タグ名からIDを引くために使う。
   *
   * @param names タグ名のリスト
   * @return タグ一覧
   */
  @Query(
      """
          SELECT *
          FROM tags
          WHERE name IN (:names)
          """)
  List<TagEntity> findByNameIn(@Param("names") List<String> names);
}
