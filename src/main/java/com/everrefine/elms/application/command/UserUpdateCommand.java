package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.user.User;
import java.time.LocalDateTime;
import java.util.UUID;

/** 更新用ユーザーのコマンド。 */
public record UserUpdateCommand(
    UUID id,
    String realName,
    String userName,
    String emailAddress,
    String thumbnailUrl,
    LocalDateTime updatedAt) {

  /**
   * Userエンティティに変換する。
   *
   * @param user 更新対象のユーザー
   * @return 更新後のユーザーエンティティ
   */
  public User toUser(User user) {
    return user.update(emailAddress, null, realName, userName, thumbnailUrl, null);
  }
}
