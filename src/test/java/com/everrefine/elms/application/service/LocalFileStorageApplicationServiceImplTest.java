package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.domain.model.user.Password;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Testcontainers
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class LocalFileStorageApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  private static Path uploadDir;

  @Autowired private FileStorageApplicationService fileStorageApplicationService;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeAll
  static void createUploadDir() throws IOException {
    uploadDir = Files.createTempDirectory("elms-file-storage-test");
  }

  @DynamicPropertySource
  static void registerUploadDirectory(DynamicPropertyRegistry registry) {
    registry.add("upload.directory", () -> uploadDir.toString());
  }

  @BeforeEach
  void setUp() throws IOException {
    if (Files.exists(uploadDir)) {
      try (Stream<Path> files = Files.list(uploadDir)) {
        files.forEach(
            path -> {
              try {
                Files.deleteIfExists(path);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
      }
    }
    jdbcTemplate.execute("DELETE FROM users");
    jdbcTemplate.execute("DELETE FROM lessons");
    jdbcTemplate.execute("DELETE FROM courses");
  }

  private void createFile(String filename) throws IOException {
    Files.writeString(uploadDir.resolve(filename), "test");
  }

  private void createUser(String thumbnailUrl) {
    jdbcTemplate.update(
        """
            INSERT INTO users (
                email_address, password, real_name, user_name,
                thumbnail_url, user_role, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
        "test@example.com",
        Password.encryptAndCreate("password").value(),
        "テスト ユーザー",
        "testuser",
        thumbnailUrl,
        "GENERAL",
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  @Nested
  class 画像アップロード {

    @Test
    void 画像を保存し保存先パスを返す() throws IOException {
      // Arrange
      MockMultipartFile file =
          new MockMultipartFile("file", "sample.png", "image/png", "dummy".getBytes());

      // Act
      String filePath = fileStorageApplicationService.saveImage(file);

      // Assert
      assertTrue(filePath.startsWith("/uploads/"));
      assertTrue(filePath.endsWith(".png"), () -> "拡張子が引き継がれていません: " + filePath);
      assertTrue(Files.exists(uploadDir.resolve(filePath.substring("/uploads/".length()))));
    }

    @Test
    void 空ファイルの場合はBadRequestExceptionを投げる() {
      // Arrange
      MockMultipartFile emptyFile =
          new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

      // Act & Assert
      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> fileStorageApplicationService.saveImage(emptyFile));
      assertEquals("ファイルを指定してください", exception.getMessage());
    }

    @Test
    void 拡張子がない場合も保存できる() throws IOException {
      // Arrange
      MockMultipartFile file =
          new MockMultipartFile("file", "noextension", "image/png", "dummy".getBytes());

      // Act
      String filePath = fileStorageApplicationService.saveImage(file);

      // Assert
      assertTrue(filePath.startsWith("/uploads/"));
      assertTrue(Files.exists(uploadDir.resolve(filePath.substring("/uploads/".length()))));
    }
  }

  @Nested
  class 孤立ファイル削除 {
    @Test
    void 孤立ファイルが削除される() throws IOException {
      createFile("orphan.jpg");

      fileStorageApplicationService.delete();

      assertFalse(Files.exists(uploadDir.resolve("orphan.jpg")));
    }

    @Test
    void DBに存在するファイルは削除されない() throws IOException {
      createFile("test.jpg");
      createUser("/uploads/test.jpg");

      fileStorageApplicationService.delete();

      assertTrue(Files.exists(uploadDir.resolve("test.jpg")));
    }

    @Test
    void アップロードディレクトリが空のとき正常終了する(CapturedOutput output) throws IOException {
      fileStorageApplicationService.delete();

      assertTrue(output.getOut().contains("削除対象のファイルは見つかりませんでした。"));
    }

    @Test
    void 孤立ファイルがないとき削除されない(CapturedOutput output) throws IOException {
      createFile("test.jpg");
      createUser("/uploads/test.jpg");

      fileStorageApplicationService.delete();

      assertTrue(output.getOut().contains("削除対象のファイルは見つかりませんでした。"));
      assertTrue(Files.exists(uploadDir.resolve("test.jpg")));
    }
  }
}
