package com.everrefine.elms.infrastructure.entity.user;

import com.everrefine.elms.domain.model.ThumbnailUrl;
import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.Password;
import com.everrefine.elms.domain.model.user.RealName;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserName;
import com.everrefine.elms.domain.model.user.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

/** ユーザーのエンティティ。 */
@Table("users")
public record UserEntity(
    @Id UUID id,
    String emailAddress,
    String password,
    String realName,
    String userName,
    @Nullable String thumbnailUrl,
    String userRole,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param user ユーザーのドメインモデル
   * @return エンティティ
   */
  public static UserEntity from(User user) {
    return new UserEntity(
        user.id(),
        user.emailAddress().value(),
        user.password().value(),
        user.realName().value(),
        user.userName().value(),
        user.thumbnailUrl() != null ? user.thumbnailUrl().value() : null,
        user.userRole().name(),
        user.createdAt(),
        user.updatedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return ユーザーのドメインモデル
   */
  public User toDomain() {
    return new User(
        id,
        new EmailAddress(emailAddress),
        new Password(password),
        new RealName(realName),
        new UserName(userName),
        thumbnailUrl != null ? new ThumbnailUrl(thumbnailUrl) : null,
        UserRole.valueOf(userRole),
        createdAt,
        updatedAt);
  }
}
