package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.passwordreset.PasswordResetToken;
import java.util.Optional;

/** パスワードリセットトークンのリポジトリインターフェース。 */
public interface PasswordResetTokenRepository {

  /**
   * パスワードリセットトークンを保存する。
   *
   * @param token 保存するパスワードリセットトークン
   * @return 保存されたパスワードリセットトークン
   */
  PasswordResetToken save(PasswordResetToken token);

  /**
   * トークン文字列でパスワードリセットトークンを取得する。
   *
   * @param token トークン文字列
   * @return パスワードリセットトークン（存在しない場合は空）
   */
  Optional<PasswordResetToken> findByToken(String token);
}
