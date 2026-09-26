package com.everrefine.elms.application.command;

/** パスワードリセットリクエスト用のコマンド。 */
public record PasswordResetRequestCommand(String emailAddress) {}
