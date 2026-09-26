package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.passwordreset.PasswordResetTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** パスワードリセットトークンのDAOインターフェース。 */
@Repository
public interface PasswordResetTokenDao extends CrudRepository<PasswordResetTokenEntity, UUID> {

  /**
   * トークン文字列でパスワードリセットトークンを取得する。
   *
   * @param token トークン文字列
   * @return パスワードリセットトークン（存在しない場合は空）
   */
  Optional<PasswordResetTokenEntity> findByToken(String token);
}
