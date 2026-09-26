package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.user.User;

/** パスワード更新用のコマンド。 */
public record PasswordUpdateCommand(String currentPassword, String newPassword) {

  /**
   * Userエンティティに変換する。
   *
   * @param user 更新対象のユーザー
   * @param newPassword 新しいパスワード（暗号化済み）
   * @return 更新後のユーザーエンティティ
   */
  public User toUser(User user, String newPassword) {
    return user.update(null, newPassword, null, null, null, null);
  }
}
