package com.everrefine.elms.domain.model.tag;

import java.util.UUID;
import org.springframework.lang.Nullable;

/** タグのドメインモデル。 */
public record Tag(@Nullable UUID id, String name) {

  /**
   * タグを作成する。
   *
   * <p>IDはDBが採番するため、保存前は {@code null} になる。保存後は {@code tags} テーブルから引いたIDが入る。
   *
   * <p>タグ名は先頭と末尾の空白を取り除いて保持する。{@code trim()} ではなく {@code strip()} を使うのは、除去対象に含めたい 全角スペース（U+3000）が
   * {@code trim()} では残ってしまうため。途中の空白は取り除かない。
   *
   * <p>大文字・小文字は区別する。recordの {@code equals} が {@link String#equals} に委譲するため、「Java」と「java」は
   * 別のタグとして扱われる。
   *
   * @param id タグID（保存前は {@code null}）
   * @param name タグ名
   */
  public Tag {
    name = name.strip();
  }
}
