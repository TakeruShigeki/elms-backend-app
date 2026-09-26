package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.passwordreset.PasswordResetToken;
import com.everrefine.elms.domain.repository.PasswordResetTokenRepository;
import com.everrefine.elms.infrastructure.dao.PasswordResetTokenDao;
import com.everrefine.elms.infrastructure.entity.passwordreset.PasswordResetTokenEntity;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

/** {@link PasswordResetTokenRepository} の実装。 */
@Repository
@AllArgsConstructor
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

  private final PasswordResetTokenDao dao;

  @Override
  public PasswordResetToken save(PasswordResetToken token) {
    return dao.save(PasswordResetTokenEntity.from(token)).toDomain();
  }

  @Override
  public Optional<PasswordResetToken> findByToken(String token) {
    return dao.findByToken(token).map(PasswordResetTokenEntity::toDomain);
  }
}
