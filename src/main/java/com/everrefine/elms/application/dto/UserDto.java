package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.model.user.UserLoginHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** ユーザーDTO。 */
public record UserDto(
    @Schema(description = "ユーザーID", example = "1") UUID id,
    @Schema(description = "メールアドレス", example = "yamada@example.com") String emailAddress,
    @Schema(description = "本名", example = "山田太郎") String realName,
    @Schema(description = "ユーザー名", example = "yamada_taro") String userName,
    @Schema(description = "サムネイルURL", example = "https://example.com/thumbnail.png")
        String thumbnailUrl,
    @Schema(description = "ユーザーロール（一般 / 管理者）", example = "一般") String userRole,
    @Schema(description = "登録日時", example = "2024-01-01T09:00:00") LocalDateTime createdAt,
    @Schema(description = "最終ログイン日時", example = "2024-06-01T10:30:00") LocalDateTime lastLoginAt,
    @Schema(description = "受講進捗率（0〜100）", example = "75.00") BigDecimal progressRate) {

  /**
   * UserエンティティからUserDtoを生成する。
   *
   * @param user ユーザーエンティティ
   * @param userLoginHistory ユーザーログイン履歴
   * @param progressRate 受講進捗率
   * @return ユーザーDTO
   */
  public static UserDto from(
      User user, UserLoginHistory userLoginHistory, BigDecimal progressRate) {

    return new UserDto(
        user.id(),
        user.emailAddress().value(),
        user.realName().value(),
        user.userName().value(),
        user.thumbnailUrl() == null ? null : user.thumbnailUrl().value(),
        user.userRole().getRoleName(),
        user.createdAt(),
        userLoginHistory == null ? null : userLoginHistory.updatedAt(),
        progressRate);
  }
}
