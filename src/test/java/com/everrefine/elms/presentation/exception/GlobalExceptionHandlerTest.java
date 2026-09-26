package com.everrefine.elms.presentation.exception;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.everrefine.elms.testsupport.TestDataFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * {@link GlobalExceptionHandler} の統合テストクラス。
 *
 * <p>コントローラーに宣言した {@code @ApiResponse} と、実際に返るHTTPステータスが一致することを検証する。 サービス層の例外型ではなく、例外ハンドラを通った後の
 * <b>実際のHTTPレスポンス</b> を確認する点が重要である。
 *
 * <p>{@code @ExceptionHandler(Exception.class)} のcatch-allは、具体的な例外を飲み込んで500にしてしまいやすい。
 * このテストはその退行を検出する。
 */
@ActiveProfiles("dev")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class GlobalExceptionHandlerTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** 検証対象へのリクエスト送信に使用するMockMvc。 */
  @Autowired private MockMvc mockMvc;

  /** テストデータ作成ヘルパー。 */
  @Autowired private TestDataFactory testData;

  /** 検証で使い回す、存在しないID。 */
  private static final UUID MISSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private UUID adminId;

  @BeforeEach
  void authenticateAsAdmin() {
    SecurityContextHolder.clearContext();
    adminId = testData.createUser("admin@example.com", "pass", "管理 者", "adminuser", "ADMIN");
    authenticateAs(adminId, "ADMIN");
  }

  /** 指定したユーザーIDと権限で認証済みの状態にする。 */
  private void authenticateAs(UUID userId, String authority) {
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new User(userId.toString(), "password", authorities), null, authorities));
  }

  @Nested
  class リクエスト不正 {

    @Test
    void パスパラメータが不正なUUIDのときステータス400が返る() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/courses/not-a-uuid"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void リクエストボディのバリデーション違反のときステータス400が返る() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void 壊れたJSONボディのときステータス400が返る() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\": "))
          .andExpect(status().isBadRequest());
    }

    @Test
    void 必須のリクエストパートがないときステータス400が返る() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.multipart("/api/files/upload"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void 現在のパスワードが一致しないときステータス400が返る() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.put("/api/users/password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"currentPassword\":\"wrongPass\",\"newPassword\":\"newPass123\"}"))
          .andExpect(status().isBadRequest());
    }

    /**
     * {@code ResponseStatusException} に指定した理由がレスポンスに残ることを検証する。
     *
     * <p>この例外は親クラスの {@code ErrorResponseException} としても処理されるため、専用ハンドラがなくてもステータスは400になる。
     * しかし理由が定型文に置き換わってしまうため、メッセージまで検証することで専用ハンドラの退行を検出する。
     */
    @Test
    void CSVに現在ログイン中ユーザーが含まれないとき理由付きでステータス400が返る() throws Exception {
      String csv = "権限,氏名,メールアドレス,ユーザー名\n管理者,山田 太郎,other@example.com,yamada\n";
      mockMvc
          .perform(
              MockMvcRequestBuilders.multipart("/api/users/import")
                  .file(
                      new MockMultipartFile(
                          "file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8))))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("現在ログイン中のユーザーがCSVに含まれていません"));
    }
  }

  @Nested
  class 認証と認可 {

    @Test
    void 未認証のときステータス401が返る() throws Exception {
      SecurityContextHolder.clearContext();
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/courses"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void 管理者権限が必要なAPIを一般ユーザーで呼ぶとステータス403が返る() throws Exception {
      authenticateAs(adminId, "GENERAL");
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/users/{userId}", MISSING_ID))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
  }

  @Nested
  class リソース未検出 {

    @Test
    void 存在しないコースを取得するとステータス404が返る() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/courses/{courseId}", MISSING_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 存在しないレッスングループにレッスンを作成するとステータス404が返る() throws Exception {
      UUID courseId = testData.createCourse(new BigDecimal("987654"), "検証コース", "説明");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(
                      "/api/courses/{courseId}/lesson-groups/{lessonGroupId}/lessons",
                      courseId,
                      MISSING_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"レッスン\",\"content\":\"本文\",\"videoUrl\":null}"))
          .andExpect(status().isNotFound());
    }

    @Test
    void 存在しないコースにレッスングループを作成するとステータス404が返る() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses/{courseId}/lesson-groups", MISSING_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"グループ\"}"))
          .andExpect(status().isNotFound());
    }

    @Test
    void ログイン中ユーザーが存在しない状態でCSV取込するとステータス404が返る() throws Exception {
      authenticateAs(MISSING_ID, "ADMIN");
      String csv = "権限,氏名,メールアドレス,ユーザー名\n管理者,山田 太郎,a@example.com,yamada\n";
      mockMvc
          .perform(
              MockMvcRequestBuilders.multipart("/api/users/import")
                  .file(
                      new MockMultipartFile(
                          "file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8))))
          .andExpect(status().isNotFound());
    }

    @Test
    void 存在しないURLのときステータス404が返る() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/unknown-path"))
          .andExpect(status().isNotFound());
    }

    @Test
    void 削除は対象が存在しなくてもステータス204が返る() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/courses/{courseId}", MISSING_ID))
          .andExpect(status().isNoContent());
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/news/{newsId}", MISSING_ID))
          .andExpect(status().isNoContent());
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/users/{userId}", MISSING_ID))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  class フレームワーク由来のエラー {

    @Test
    void サポートされないHTTPメソッドのときステータス405が返る() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.patch("/api/courses"))
          .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void サポートされないContentTypeのときステータス415が返る() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses")
                  .contentType(MediaType.TEXT_PLAIN)
                  .content("hello"))
          .andExpect(status().isUnsupportedMediaType());
    }
  }
}
