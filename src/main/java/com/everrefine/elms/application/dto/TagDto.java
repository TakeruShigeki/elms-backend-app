package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.tag.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** タグのDTO。 */
public record TagDto(
    @Schema(description = "タグID", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
    @Schema(description = "タグ名", example = "Java") String name) {

  /**
   * TagエンティティからTagDtoを生成する。
   *
   * @param tag タグのドメインモデル
   * @return タグDTO
   */
  public static TagDto from(Tag tag) {
    return new TagDto(tag.id(), tag.name());
  }
}
