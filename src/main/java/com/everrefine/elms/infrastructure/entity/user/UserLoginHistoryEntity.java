package com.everrefine.elms.infrastructure.entity.user;

import com.everrefine.elms.domain.model.user.UserLoginHistory;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** ユーザーログイン履歴のエンティティ。 */
@Table("user_login_histories")
public record UserLoginHistoryEntity(
    @Id UUID id, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param userLoginHistory ユーザログイン履歴のドメインモデル
   * @return エンティティ
   */
  public static UserLoginHistoryEntity from(UserLoginHistory userLoginHistory) {
    return new UserLoginHistoryEntity(
        userLoginHistory.id(),
        userLoginHistory.userId(),
        userLoginHistory.createdAt(),
        userLoginHistory.updatedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return ユーザログイン履歴のドメインモデル
   */
  public UserLoginHistory toDomain() {
    return new UserLoginHistory(id, userId, createdAt, updatedAt);
  }
}
