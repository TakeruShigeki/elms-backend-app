package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.everrefine.elms.domain.model.user.Password;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ActiveProfiles("prd")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@TestPropertySource(
    properties = {
      "JWT_SECRET=0dTIXBDkSbhltNOJX8M3oMuN5K+vDG4Z7ZaY8rpg8kQ=",
      "DB_HOST=localhost",
      "DB_PORT=5432",
      "DB_NAME=test",
      "DB_USER=test",
      "DB_PASS=test",
      "S3_BUCKET_NAME=elms-uploads",
      "cloud.aws.s3.public-url=http://localhost:4566/elms-uploads",
      "cloud.aws.s3.endpoint=http://localhost:4566",
      "AWS_ACCESS_KEY_ID=test",
      "AWS_SECRET_ACCESS_KEY=test",
      "BASE_URL=http://localhost:3000",
      "CORS_ALLOWED_ORIGINS=http://localhost:3000"
    })
@Testcontainers
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class S3FileStorageApplicationServiceImplTest {

  private static final String S3_PUBLIC_URL = "http://localhost:4566/elms-uploads";
  private static final String BUCKET = "elms-uploads";

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  @MockitoBean private S3Client s3Client;

  @Autowired private FileStorageApplicationService fileStorageApplicationService;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute("DELETE FROM users");
    jdbcTemplate.execute("DELETE FROM lessons");
    jdbcTemplate.execute("DELETE FROM courses");
  }

  private void mockS3Objects(String... keys) {
    List<S3Object> objects =
        java.util.Arrays.stream(keys).map(key -> S3Object.builder().key(key).build()).toList();
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(ListObjectsV2Response.builder().contents(objects).isTruncated(false).build());
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
  class 孤立ファイル削除 {
    @Test
    void 孤立ファイルが削除される() throws IOException {
      mockS3Objects("orphan.jpg");

      fileStorageApplicationService.delete();

      verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void DBに存在するファイルは削除されない() throws IOException {
      mockS3Objects("test.jpg");
      createUser(S3_PUBLIC_URL + "/test.jpg");

      fileStorageApplicationService.delete();

      verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void S3バケットにファイルがないとき正常終了する(CapturedOutput output) throws IOException {
      mockS3Objects();

      fileStorageApplicationService.delete();

      assertTrue(output.getOut().contains("S3バケットにファイルがありません。"));
    }

    @Test
    void 孤立ファイルがないとき削除処理が呼ばれない(CapturedOutput output) throws IOException {
      mockS3Objects("test.jpg");
      createUser(S3_PUBLIC_URL + "/test.jpg");

      fileStorageApplicationService.delete();

      assertTrue(output.getOut().contains("削除対象のファイルは見つかりませんでした。"));
      verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void S3削除に失敗したとき警告ログが出力されて正常に終了する(CapturedOutput output) throws IOException {
      mockS3Objects("fail.jpg");
      when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
          .thenThrow(S3Exception.builder().message("error").build());

      fileStorageApplicationService.delete();

      assertTrue(output.getOut().contains("未使用ファイルの削除に失敗しました。key: fail.jpg"));
      assertTrue(output.getOut().contains("1件の削除に失敗しました。"));
    }

    @Test
    void S3削除に失敗したとき他のファイルは削除される() throws IOException {
      mockS3Objects("fail.jpg", "success.jpg");
      when(s3Client.deleteObject(
              DeleteObjectRequest.builder().bucket(BUCKET).key("fail.jpg").build()))
          .thenThrow(S3Exception.builder().message("error").build());

      fileStorageApplicationService.delete();

      verify(s3Client)
          .deleteObject(DeleteObjectRequest.builder().bucket(BUCKET).key("success.jpg").build());
    }
  }
}
