package com.everrefine.elms.application.command;

/** パスワードリセット確定用のコマンド。 */
public record PasswordResetConfirmCommand(String token, String newPassword) {}
