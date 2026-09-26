package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserName;
import com.everrefine.elms.domain.model.user.UserSearchCondition;
import com.everrefine.elms.domain.repository.UserRepository;
import com.everrefine.elms.infrastructure.dao.UserDao;
import com.everrefine.elms.infrastructure.entity.user.UserEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final UserDao userDao;
  private final JdbcAggregateTemplate jdbcAggregateTemplate;

  @Override
  public List<User> findUsersByIds(List<UUID> userIds) {
    if (userIds.isEmpty()) {
      return Collections.emptyList();
    }
    return userDao.findByIdIn(userIds).stream().map(UserEntity::toDomain).toList();
  }

  @Override
  public User updateUser(User user) {
    return userDao.save(UserEntity.from(user)).toDomain();
  }

  @Override
  public User createUser(User user) {
    return userDao.save(UserEntity.from(user)).toDomain();
  }

  @Override
  public void deleteUserById(UUID id) {
    userDao.deleteById(id);
  }

  @Override
  public Optional<User> findUserById(UUID id) {
    return userDao.findById(id).map(UserEntity::toDomain);
  }

  @Override
  public Optional<User> findUserByEmailAddress(EmailAddress emailAddress) {
    return userDao.findByEmailAddress(emailAddress.value()).map(UserEntity::toDomain);
  }

  @Override
  public Optional<User> findUserByUserName(UserName userName) {
    return userDao.findByUserName(userName.value()).map(UserEntity::toDomain);
  }

  @Override
  public List<UUID> findUserIdsBySearchConditions(UserSearchCondition userSearchCondition) {
    return userDao.findUserIdsBySearchConditions(
        userSearchCondition.userId(),
        userSearchCondition.userRole(),
        userSearchCondition.realName(),
        userSearchCondition.userName(),
        userSearchCondition.emailAddress(),
        userSearchCondition.createdDateFrom() == null
            ? null
            : userSearchCondition.createdDateFrom(),
        userSearchCondition.createdDateTo() == null ? null : userSearchCondition.createdDateTo(),
        userSearchCondition.getPageSize(),
        userSearchCondition.getOffset());
  }

  @Override
  public int countUsers(UserSearchCondition userSearchCondition) {
    return userDao.countUsersBySearchConditions(
        userSearchCondition.userId(),
        userSearchCondition.userRole(),
        userSearchCondition.realName(),
        userSearchCondition.userName(),
        userSearchCondition.emailAddress(),
        userSearchCondition.createdDateFrom() == null
            ? null
            : userSearchCondition.createdDateFrom(),
        userSearchCondition.createdDateTo() == null ? null : userSearchCondition.createdDateTo());
  }

  @Override
  public List<User> findAllByOrderByCreatedAtAscIdAsc() {
    return userDao.findAllByOrderByCreatedAtAscIdAsc().stream().map(UserEntity::toDomain).toList();
  }

  @Override
  public List<String> findByThumbnailUrlStartingWith(String prefix) {
    return userDao.findByThumbnailUrlStartingWith(prefix);
  }

  @Override
  public void deleteAllUsers() {
    userDao.deleteAll();
  }

  /**
   * 複数のユーザーを一括登録する。
   *
   * <p>IDは呼び出し側で採番済みであること。IDが確定していると、Spring Data JDBCが採番結果の問い合わせを行わないため、 JDBCドライバの {@code
   * reWriteBatchedInserts} が複数レコードを1つのINSERT文にまとめられる。
   *
   * @param users 登録するユーザーリスト（IDは採番済み）
   * @return 登録されたユーザーリスト
   */
  @Override
  public List<User> saveAllUsers(List<User> users) {
    if (users.isEmpty()) {
      return users;
    }

    jdbcAggregateTemplate.insertAll(users.stream().map(UserEntity::from).toList());

    return users;
  }
}
