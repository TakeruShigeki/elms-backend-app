package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.user.UserSearchCondition;
import java.time.LocalDate;

/** ユーザー検索用のコマンド。 */
public record UserSearchCommand(
    int pageNum,
    int pageSize,
    String userId,
    String userRole,
    String realName,
    String userName,
    String emailAddress,
    LocalDate createdDateFrom,
    LocalDate createdDateTo) {

  /**
   * UserSearchConditionに変換する。
   *
   * @return ユーザー検索条件
   */
  public UserSearchCondition toSearchCondition() {
    return new UserSearchCondition(
        pageNum,
        pageSize,
        userId,
        userRole,
        realName,
        userName,
        emailAddress,
        createdDateFrom,
        createdDateTo);
  }
}
