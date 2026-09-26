package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.user.UserEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** ユーザーのDAOインターフェース。 */
@Repository
public interface UserDao extends CrudRepository<UserEntity, UUID> {

  @Query(
      """
      SELECT *
      FROM users
      WHERE email_address = :emailAddress
      """)
  Optional<UserEntity> findByEmailAddress(@Param("emailAddress") String emailAddress);

  @Query(
      """
      SELECT *
      FROM users
      WHERE user_name = :userName
      """)
  Optional<UserEntity> findByUserName(@Param("userName") String userName);

  @Query(
      """
          SELECT *
          FROM users
          WHERE email_address = :emailAddress
          And password = :password
          """)
  Optional<UserEntity> findByEmailAddressAndPassword(
      @Param("emailAddress") String emailAddress, @Param("password") String password);

  @Query(
      """
          SELECT *
          FROM users
          WHERE id IN (:ids)
          ORDER BY created_at DESC
          """)
  List<UserEntity> findByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      """
          SELECT id FROM users
          WHERE (:userId IS NULL OR :userId = '' OR CAST(id AS TEXT) LIKE CONCAT('%', :userId, '%'))
            AND (:userRole IS NULL OR :userRole = '' OR user_role = :userRole)
            AND (:realName IS NULL OR :realName = '' OR real_name LIKE CONCAT('%', :realName, '%'))
            AND (:userName IS NULL OR :userName = '' OR user_name LIKE CONCAT('%', :userName, '%'))
            AND (:emailAddress IS NULL OR :emailAddress = '' OR email_address LIKE CONCAT('%', :emailAddress, '%'))
            AND (CAST(:createdDateFrom AS DATE) IS NULL OR created_at >=  CAST(:createdDateFrom AS DATE))
            AND (CAST(:createdDateTo AS DATE) IS NULL OR created_at < CAST(:createdDateTo AS DATE) + INTERVAL '1 day')
          ORDER BY created_at DESC
          LIMIT :pageSize
          OFFSET :offset
          """)
  List<UUID> findUserIdsBySearchConditions(
      @Param("userId") String userId,
      @Param("userRole") String userRole,
      @Param("realName") String realName,
      @Param("userName") String userName,
      @Param("emailAddress") String emailAddress,
      @Param("createdDateFrom") LocalDate createdDateFrom,
      @Param("createdDateTo") LocalDate createdDateTo,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  @Query(
      """
          SELECT COUNT(*)
          FROM users
          WHERE (:userId IS NULL OR :userId = '' OR CAST(id AS TEXT) LIKE CONCAT('%', :userId, '%'))
            AND (:userRole IS NULL OR :userRole = '' OR user_role = :userRole)
            AND (:realName IS NULL OR :realName = '' OR real_name LIKE CONCAT('%', :realName, '%'))
            AND (:userName IS NULL OR :userName = '' OR user_name LIKE CONCAT('%', :userName, '%'))
            AND (:emailAddress IS NULL OR :emailAddress = '' OR email_address LIKE CONCAT('%', :emailAddress, '%'))
            AND (CAST(:createdDateFrom AS DATE) IS NULL OR created_at >= CAST(:createdDateFrom AS DATE))
            AND (CAST(:createdDateTo AS DATE) IS NULL OR created_at < CAST(:createdDateTo AS DATE) + INTERVAL '1 day')
          """)
  int countUsersBySearchConditions(
      @Param("userId") String userId,
      @Param("userRole") String userRole,
      @Param("realName") String realName,
      @Param("userName") String userName,
      @Param("emailAddress") String emailAddress,
      @Param("createdDateFrom") LocalDate createdDateFrom,
      @Param("createdDateTo") LocalDate createdDateTo);

  List<UserEntity> findAllByOrderByCreatedAtAscIdAsc();

  @Query(
      """
          SELECT thumbnail_url
          FROM users
          WHERE thumbnail_url LIKE CONCAT(:prefix, '%')
          """)
  List<String> findByThumbnailUrlStartingWith(String prefix);

  /** 全ユーザーを削除する。 */
  @Modifying
  @Query("DELETE FROM users")
  void deleteAll();
}
