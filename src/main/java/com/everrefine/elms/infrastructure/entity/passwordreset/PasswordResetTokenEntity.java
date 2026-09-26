package com.everrefine.elms.infrastructure.entity.passwordreset;

import com.everrefine.elms.domain.model.passwordreset.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

/** パスワードリセットトークンのエンティティ。 */
@Table("password_reset_tokens")
public record PasswordResetTokenEntity(
    @Id UUID id,
    UUID userId,
    String token,
    LocalDateTime expiresAt,
    @Nullable LocalDateTime usedAt) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param passwordResetToken パスワードリセットトークンのドメインモデル
   * @return エンティティ
   */
  public static PasswordResetTokenEntity from(PasswordResetToken passwordResetToken) {
    return new PasswordResetTokenEntity(
        passwordResetToken.id(),
        passwordResetToken.userId(),
        passwordResetToken.token(),
        passwordResetToken.expiresAt(),
        passwordResetToken.usedAt());
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return パスワードリセットトークンのドメインモデル
   */
  public PasswordResetToken toDomain() {
    return new PasswordResetToken(id, userId, token, expiresAt, usedAt);
  }
}
