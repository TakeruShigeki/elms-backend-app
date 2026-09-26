package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserName;
import com.everrefine.elms.domain.model.user.UserSearchCondition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** ユーザーのリポジトリインターフェース。 */
public interface UserRepository {

  /**
   * IDでユーザーを取得する。
   *
   * @param id ユーザーID
   * @return ユーザー（存在しない場合は空）
   */
  Optional<User> findUserById(UUID id);

  /**
   * 複数IDでユーザー一覧を取得する。
   *
   * @param ids ユーザーIDリスト
   * @return ユーザーリスト
   */
  List<User> findUsersByIds(List<UUID> ids);

  /**
   * 検索条件に合致するユーザーIDリストを取得する。
   *
   * @param userSearchCondition 検索条件
   * @return ユーザーIDリスト
   */
  List<UUID> findUserIdsBySearchConditions(UserSearchCondition userSearchCondition);

  /**
   * メールアドレスでユーザーを取得する。
   *
   * @param emailAddress メールアドレス
   * @return ユーザー（存在しない場合は空）
   */
  Optional<User> findUserByEmailAddress(EmailAddress emailAddress);

  /**
   * ユーザー名でユーザーを取得する。
   *
   * @param userName ユーザー名
   * @return ユーザー（存在しない場合は空）
   */
  Optional<User> findUserByUserName(UserName userName);

  /**
   * 検索条件に合致するユーザー数を取得する。
   *
   * @param userSearchCondition 検索条件
   * @return ユーザー数
   */
  int countUsers(UserSearchCondition userSearchCondition);

  /**
   * ユーザーを登録する。
   *
   * @param user 登録するユーザー
   * @return 登録されたユーザー
   */
  User createUser(User user);

  /**
   * ユーザーを更新する。
   *
   * @param user 更新するユーザー
   * @return 更新されたユーザー
   */
  User updateUser(User user);

  /**
   * IDでユーザーを削除する。
   *
   * @param id ユーザーID
   */
  void deleteUserById(UUID id);

  /**
   * 全ユーザーを作成日時昇順で取得する。
   *
   * @return ユーザーリスト
   */
  List<User> findAllByOrderByCreatedAtAscIdAsc();

  /**
   * 指定プレフィックスで始まるサムネイルURLを取得する。
   *
   * @param prefix URLプレフィックス
   * @return サムネイルURLリスト
   */
  List<String> findByThumbnailUrlStartingWith(String prefix);

  /** 全ユーザーを削除する。 */
  void deleteAllUsers();

  /**
   * 複数のユーザーを一括登録する。
   *
   * <p>IDは呼び出し側で採番済みであること。
   *
   * @param users 登録するユーザーリスト（IDは採番済み）
   * @return 登録されたユーザーリスト
   */
  List<User> saveAllUsers(List<User> users);
}
