package com.everrefine.elms.domain.service;

/** ユーザーに関するドメインサービスインターフェース。 */
public interface UserDomainService {

  /**
   * パスワードと確認用パスワードが合致するかを判定する。
   *
   * @param password パスワード
   * @param confirmPassword 確認用パスワード
   * @return ture: 合致する, false: 合致しない
   */
  boolean matchesPassword(String password, String confirmPassword);
}
