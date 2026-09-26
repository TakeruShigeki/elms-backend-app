package com.everrefine.elms.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.everrefine.elms.domain.model.user.UserRole;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** {@link UserImportCommand} の単体テスト。 */
class UserImportCommandTest {

  private static final String HEADER = "権限,氏名,メールアドレス,ユーザー名";

  private static MultipartFile csvFile(String filename, String content) {
    return new MockMultipartFile(
        "file", filename, "text/csv", content.getBytes(StandardCharsets.UTF_8));
  }

  @Nested
  class CSV読み込み {
    @Test
    void BOM付きCSVとクォート付きカンマを読み込める() {
      String csv =
          "\uFEFF" + String.join("\n", HEADER, "一般,\"テスト, 太郎\",test@example.com,\"user,name\"");
      MultipartFile file = csvFile("users.csv", csv);

      UserImportCommand command = UserImportCommand.from(file, UUID.randomUUID());

      assertEquals(1, command.getImportCount());
      UserImportRowCommand row = command.rows().get(0);
      assertEquals(UserRole.GENERAL, row.userRole());
      assertEquals("テスト, 太郎", row.realName());
      assertEquals("test@example.com", row.emailAddress());
      assertEquals("user,name", row.userName());
    }

    @Test
    void ヘッダのみCSVはCommand生成上は許容される() {
      MultipartFile file = csvFile("users.csv", HEADER);

      UserImportCommand command = UserImportCommand.from(file, UUID.randomUUID());

      assertEquals(0, command.getImportCount());
    }
  }

  @Nested
  class CSVバリデーション {
    @Test
    void CSVファイル形式が不正な場合ResponseStatusExceptionを投げる() {
      MultipartFile file = csvFile("users.txt", "test content");

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      assertEquals("CSVファイル形式が不正です", exception.getReason());
    }

    @Test
    void ヘッダ不正時はreasonが維持されたResponseStatusExceptionを投げる() {
      String csv = String.join("\n", "不正ヘッダ,氏名,メールアドレス,ユーザー名", "一般,山田太郎,t@example.com,taro");
      MultipartFile file = csvFile("users.csv", csv);

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      assertEquals("CSVヘッダが不正です", exception.getReason());
    }

    @Test
    void 空行を挟んでも物理行番号を示すResponseStatusExceptionを投げる() {
      String csv = String.join("\n", HEADER, "", "一般,,t@example.com,taro");
      MultipartFile file = csvFile("users.csv", csv);

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      assertEquals("行3: 必須項目が入力されていません", exception.getReason());
    }

    @Test
    void 未クローズクォートは列数不正のResponseStatusExceptionを投げる() {
      String csv = String.join("\n", HEADER, "一般,\"山田太郎,t@example.com,taro");
      MultipartFile file = csvFile("users.csv", csv);

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      assertEquals("行2: 列数が不正です", exception.getReason());
    }

    @Test
    void メールアドレスの形式が不正な場合は行番号付きの400が返る() {
      String csv = String.join("\n", HEADER, "一般,山田 太郎,notanemail,yamada");
      MultipartFile file = csvFile("users.csv", csv);

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      assertEquals("行2: 不正なメールアドレスです: notanemail", exception.getReason());
    }

    @Test
    void 氏名が文字数制限を超える場合は行番号付きの400が返る() {
      String tooLongRealName = "あ".repeat(51);
      String csv = String.join("\n", HEADER, "一般," + tooLongRealName + ",test@example.com,yamada");
      MultipartFile file = csvFile("users.csv", csv);

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      assertEquals("行2: 氏名は50文字以内で入力してください", exception.getReason());
    }

    @Test
    void ユーザー名に全角文字が含まれる場合は行番号付きの400が返る() {
      String csv = String.join("\n", HEADER, "一般,山田 太郎,test@example.com,ヤマダ");
      MultipartFile file = csvFile("users.csv", csv);

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
      assertEquals("行2: ユーザー名に全角文字は使用できません", exception.getReason());
    }
  }
}
