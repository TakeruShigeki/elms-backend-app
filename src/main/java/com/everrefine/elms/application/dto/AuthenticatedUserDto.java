package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.user.User;
import java.util.UUID;

/** Spring Security 認証用のユーザーDTO。 */
public record AuthenticatedUserDto(UUID id, String encodedPassword, String roleCode) {

  /**
   * UserエンティティからAuthenticatedUserDtoを生成する。
   *
   * @param user ユーザーエンティティ
   * @return 認証用ユーザーDTO
   */
  public static AuthenticatedUserDto from(User user) {
    return new AuthenticatedUserDto(user.id(), user.password().value(), user.userRole().getCode());
  }
}
