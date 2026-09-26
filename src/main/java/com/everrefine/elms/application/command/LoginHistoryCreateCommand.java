package com.everrefine.elms.application.command;

import jakarta.validation.constraints.NotNull;

/** ログイン履歴作成用のコマンド。 */
public record LoginHistoryCreateCommand(@NotNull String email) {}
