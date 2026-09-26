package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.user.UserLoginHistoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** ユーザーログイン履歴のDAOインターフェース。 */
@Repository
public interface UserLoginHistoryDao extends CrudRepository<UserLoginHistoryEntity, UUID> {
  Optional<UserLoginHistoryEntity> findByUserId(UUID userId);

  @Query(
      """
          SELECT DISTINCT ON (user_id) *
          FROM user_login_histories
          WHERE user_id IN (:userIds)
          ORDER BY user_id, updated_at DESC
          """)
  List<UserLoginHistoryEntity> findByUserIds(@Param("userIds") List<UUID> userIds);
}
