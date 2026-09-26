package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import com.everrefine.elms.application.command.PasswordResetRequestCommand;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.repository.UserRepository;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
// JavaMailSenderをモックに差し替えるとActuatorのメールヘルスチェックが実体を見つけられず
// コンテキスト起動に失敗するため、テストでは無効化する。
@TestPropertySource(properties = "management.health.mail.enabled=false")
@Testcontainers
@Transactional
class PasswordResetApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** メールサーバーに依存しないよう送信処理はモックする。 */
  @MockitoBean private JavaMailSender mailSender;

  @Autowired private PasswordResetApplicationService passwordResetApplicationService;
  @Autowired private UserRepository userRepository;
  @Autowired private TestDataFactory testData;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void deleteUsers() {
    jdbcTemplate.execute("DELETE FROM users");
  }

  private int countTokens(UUID userId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM password_reset_tokens WHERE user_id = ?", Integer.class, userId);
    assertNotNull(count);
    return count;
  }

  private String findTokenValue(UUID userId) {
    return jdbcTemplate.queryForObject(
        "SELECT token FROM password_reset_tokens WHERE user_id = ?", String.class, userId);
  }

  /** 有効期限・使用済みを任意に指定してトークンを作成する。 */
  private String insertToken(UUID userId, LocalDateTime expiresAt, LocalDateTime usedAt) {
    String token = UUID.randomUUID().toString();
    jdbcTemplate.update(
        "INSERT INTO password_reset_tokens (user_id, token, expires_at, used_at) VALUES (?, ?, ?, ?)",
        userId,
        token,
        expiresAt,
        usedAt);
    return token;
  }

  @Nested
  class パスワードリセット申請 {

    @Test
    void 登録済みのメールアドレスならトークンが発行されメールが送信される() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");

      // Act
      passwordResetApplicationService.requestPasswordReset(
          new PasswordResetRequestCommand("yamada@example.com"));

      // Assert
      assertEquals(1, countTokens(userId));
      verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void 発行されるトークンの有効期限が30分後である() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      LocalDateTime before = LocalDateTime.now();

      // Act
      passwordResetApplicationService.requestPasswordReset(
          new PasswordResetRequestCommand("yamada@example.com"));

      // Assert
      LocalDateTime expiresAt =
          jdbcTemplate.queryForObject(
              "SELECT expires_at FROM password_reset_tokens WHERE user_id = ?",
              LocalDateTime.class,
              userId);
      assertNotNull(expiresAt);
      assertTrue(expiresAt.isAfter(before.plusMinutes(29)));
      assertTrue(expiresAt.isBefore(before.plusMinutes(31)));
    }

    /** アカウントの存在を秘匿するため、未登録でもエラーにしない。 */
    @Test
    void 未登録のメールアドレスでもエラーにならずメールも送信されない() {
      // Act
      passwordResetApplicationService.requestPasswordReset(
          new PasswordResetRequestCommand("notfound@example.com"));

      // Assert
      Integer count =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM password_reset_tokens", Integer.class);
      assertEquals(0, count);
      verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void メールアドレスとして解釈できない値でもエラーにならない() {
      // Act
      passwordResetApplicationService.requestPasswordReset(
          new PasswordResetRequestCommand("notanemail"));

      // Assert
      Integer count =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM password_reset_tokens", Integer.class);
      assertEquals(0, count);
      verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    /** 既存トークンを無効化しない仕様であることを固定する。 */
    @Test
    void 連続して申請すると有効なトークンが複数並存する() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      PasswordResetRequestCommand command = new PasswordResetRequestCommand("yamada@example.com");

      // Act
      passwordResetApplicationService.requestPasswordReset(command);
      passwordResetApplicationService.requestPasswordReset(command);

      // Assert
      assertEquals(2, countTokens(userId));
      Integer unusedCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM password_reset_tokens WHERE user_id = ? AND used_at IS NULL",
              Integer.class,
              userId);
      assertEquals(2, unusedCount);
    }
  }

  @Nested
  class パスワードリセット確定 {

    @Test
    void パスワードが更新されトークンが使用済みになる() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      passwordResetApplicationService.requestPasswordReset(
          new PasswordResetRequestCommand("yamada@example.com"));
      String token = findTokenValue(userId);
      String beforePassword = userRepository.findUserById(userId).orElseThrow().password().value();

      // Act
      String emailAddress =
          passwordResetApplicationService.confirmPasswordReset(
              new PasswordResetConfirmCommand(token, "newPassword123"));

      // Assert
      assertEquals("yamada@example.com", emailAddress);

      User updatedUser = userRepository.findUserById(userId).orElseThrow();
      assertNotEquals(beforePassword, updatedUser.password().value());
      assertTrue(
          new BCryptPasswordEncoder().matches("newPassword123", updatedUser.password().value()),
          "新しいパスワードでハッシュが更新されていません");

      LocalDateTime usedAt =
          jdbcTemplate.queryForObject(
              "SELECT used_at FROM password_reset_tokens WHERE token = ?",
              LocalDateTime.class,
              token);
      assertNotNull(usedAt);
    }

    @Test
    void 存在しないトークンの場合はIllegalArgumentExceptionを投げる() {
      // Act & Assert
      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand(UUID.randomUUID().toString(), "newPassword123");
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> passwordResetApplicationService.confirmPasswordReset(command));
      assertEquals("無効なトークンです", exception.getMessage());
    }

    @Test
    void 有効期限切れのトークンの場合はIllegalArgumentExceptionを投げる() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      String token = insertToken(userId, LocalDateTime.now().minusMinutes(1), null);

      // Act & Assert
      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand(token, "newPassword123");
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> passwordResetApplicationService.confirmPasswordReset(command));
      assertEquals("トークンの有効期限が切れています", exception.getMessage());
    }

    @Test
    void 使用済みのトークンの場合はIllegalArgumentExceptionを投げる() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      String token =
          insertToken(
              userId, LocalDateTime.now().plusMinutes(30), LocalDateTime.now().minusDays(1));

      // Act & Assert
      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand(token, "newPassword123");
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> passwordResetApplicationService.confirmPasswordReset(command));
      assertEquals("このトークンはすでに使用されています", exception.getMessage());
    }

    @Test
    void 同じトークンで2回目のリセットはできない() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      String token = insertToken(userId, LocalDateTime.now().plusMinutes(30), null);
      passwordResetApplicationService.confirmPasswordReset(
          new PasswordResetConfirmCommand(token, "newPassword123"));

      // Act & Assert
      PasswordResetConfirmCommand secondCommand =
          new PasswordResetConfirmCommand(token, "anotherPassword456");
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> passwordResetApplicationService.confirmPasswordReset(secondCommand));
      assertEquals("このトークンはすでに使用されています", exception.getMessage());
    }

    @Test
    void リセット完了メールが送信される() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      String token = insertToken(userId, LocalDateTime.now().plusMinutes(30), null);

      // Act
      passwordResetApplicationService.confirmPasswordReset(
          new PasswordResetConfirmCommand(token, "newPassword123"));

      // Assert
      verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void トークンに紐づくユーザーが存在しない場合はIllegalArgumentExceptionを投げる() {
      // Arrange - ユーザーを作成してトークンを発行した後、ユーザーだけを削除する
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      String token = insertToken(userId, LocalDateTime.now().plusMinutes(30), null);
      jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);

      // Assert - ユーザー削除に伴いトークンも削除されるため、無効なトークンとして扱われる
      assertEquals(0, countTokens(userId));

      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand(token, "newPassword123");
      assertThrows(
          IllegalArgumentException.class,
          () -> passwordResetApplicationService.confirmPasswordReset(command));
    }
  }
}
