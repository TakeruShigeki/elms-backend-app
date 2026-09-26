package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.user.UserLoginHistory;
import com.everrefine.elms.domain.repository.UserLoginHistoryRepository;
import com.everrefine.elms.infrastructure.dao.UserLoginHistoryDao;
import com.everrefine.elms.infrastructure.entity.user.UserLoginHistoryEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

/** {@link UserLoginHistoryRepository} の実装。 */
@Repository
@AllArgsConstructor
public class UserLoginHistoryRepositoryImpl implements UserLoginHistoryRepository {

  private final UserLoginHistoryDao userLoginHistoryDao;

  @Override
  public Optional<UserLoginHistory> findUserLoginHistoryByUserId(UUID userId) {
    return userLoginHistoryDao.findByUserId(userId).map(UserLoginHistoryEntity::toDomain);
  }

  @Override
  public List<UserLoginHistory> findByUserIds(List<UUID> userIds) {
    if (userIds.isEmpty()) {
      return Collections.emptyList();
    }
    return userLoginHistoryDao.findByUserIds(userIds).stream()
        .map(UserLoginHistoryEntity::toDomain)
        .toList();
  }

  @Override
  public Map<UUID, UserLoginHistory> findByUserIdsAsMap(List<UUID> userIds) {
    if (userIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return userLoginHistoryDao.findByUserIds(userIds).stream()
        .map(UserLoginHistoryEntity::toDomain)
        .collect(Collectors.toMap(UserLoginHistory::userId, history -> history, (h1, h2) -> h1));
  }

  @Override
  public void save(UserLoginHistory userLoginHistory) {
    userLoginHistoryDao.save(UserLoginHistoryEntity.from(userLoginHistory));
  }
}
