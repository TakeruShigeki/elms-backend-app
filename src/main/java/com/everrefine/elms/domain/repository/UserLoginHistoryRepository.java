package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.user.UserLoginHistory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** ユーザーログイン履歴のリポジトリインターフェース。 */
public interface UserLoginHistoryRepository {

  /**
   * ユーザーIDでログイン履歴を取得する。
   *
   * @param id ユーザーID
   * @return ログイン履歴（存在しない場合は空）
   */
  Optional<UserLoginHistory> findUserLoginHistoryByUserId(UUID id);

  /**
   * 複数のユーザーIDでログイン履歴一覧を取得する。
   *
   * @param userIds ユーザーIDのリスト
   * @return ログイン履歴一覧
   */
  List<UserLoginHistory> findByUserIds(List<UUID> userIds);

  /**
   * 複数のユーザーIDでログイン履歴をMap形式で取得する。
   *
   * @param userIds ユーザーIDのリスト
   * @return ユーザーIDをキー、ログイン履歴を値とするMap
   */
  Map<UUID, UserLoginHistory> findByUserIdsAsMap(List<UUID> userIds);

  /**
   * ログイン履歴を保存する。
   *
   * @param userLoginHistory 保存するログイン履歴
   */
  void save(UserLoginHistory userLoginHistory);
}
