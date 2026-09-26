package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import com.everrefine.elms.application.command.PasswordResetRequestCommand;

/** パスワードリセットアプリケーションサービスのインターフェース。 */
public interface PasswordResetApplicationService {

  /**
   * パスワードリセットをリクエストする。
   *
   * @param command パスワードリセットリクエストCommand
   */
  void requestPasswordReset(PasswordResetRequestCommand command);

  /**
   * パスワードを更新し、自動ログイン用にメールアドレスを返す。
   *
   * @param command パスワードリセット確定Command
   * @return 認証用メールアドレス
   */
  String confirmPasswordReset(PasswordResetConfirmCommand command);
}
