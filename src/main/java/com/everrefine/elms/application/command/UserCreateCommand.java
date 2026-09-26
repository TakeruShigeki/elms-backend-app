package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserRole;
import java.util.UUID;

/** 新規作成用ユーザーのコマンド。 */
public record UserCreateCommand(
    UUID id,
    String realName,
    String userName,
    String emailAddress,
    String password,
    String confirmPassword,
    String thumbnailUrl,
    String userRole) {

  /**
   * Userエンティティに変換する。
   *
   * @return ユーザーエンティティ
   */
  public User toUser() {
    return User.create(
        emailAddress, password, realName, userName, thumbnailUrl, UserRole.valueOf(userRole));
  }
}
