package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.dto.NewsDto;
import com.everrefine.elms.application.dto.NewsPageDto;
import com.everrefine.elms.presentation.request.NewsCreateRequest;
import com.everrefine.elms.presentation.request.NewsSearchRequest;
import com.everrefine.elms.presentation.request.NewsUpdateRequest;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE) // WebまわりのConfigurationはBean生成を無効にして高速化する。
@Testcontainers // DBはDockerコンテナを使用する。
@Transactional // 各テストメソッド終了時にテストデータをロールバックする。
public class NewsApplicationServiceImplTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** テスト対象のサービスクラス。 */
  @Autowired private NewsApplicationServiceImpl newsApplicationService;

  /** データ検証で使用するためのJdbcTemplate。 */
  @Autowired private JdbcTemplate jdbcTemplate;

  /** 作成日を指定してお知らせを登録する。並び順と日付範囲の検証に使うため created_at / updated_at を明示する。 */
  private void insertNewsWithDate(String title, String content, LocalDate createdDate) {
    LocalDateTime createdAt = createdDate.atTime(10, 0, 0);
    jdbcTemplate.update(
        """
            INSERT INTO news (title, content, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            """,
        title,
        content,
        createdAt,
        createdAt);
  }

  @Nested
  class お知らせ作成 {
    @Test
    void お知らせを新規作成できる() {
      // Arrange
      NewsCreateRequest request = new NewsCreateRequest("テストタイトル", "テスト本文");
      newsApplicationService.createNews(request.toCommand());

      // 作成されたIDを取得する。
      UUID id =
          jdbcTemplate.queryForObject("SELECT id FROM news WHERE title = ?", UUID.class, "テストタイトル");
      assertNotNull(id);

      // Act
      NewsDto dto = newsApplicationService.findNewsById(id);

      // Assert
      assertEquals(id, dto.id());
      assertEquals("テストタイトル", dto.title());
      assertEquals("テスト本文", dto.content());
    }
  }

  @Nested
  class お知らせ取得 {
    @Test
    void タイトル部分一致と日付範囲でページング降順に取得できる() {
      // Arrange
      // データ5件投入（作成日をバラして後で日付範囲・並び順テストに使う）
      insertNewsWithDate("Java入門", "A", LocalDate.of(2025, 1, 1)); // 範囲外（後で除外される）
      insertNewsWithDate("Spring解説", "B", LocalDate.of(2025, 2, 1)); // タイトル不一致（後で除外）
      insertNewsWithDate("Java実践", "C", LocalDate.of(2025, 3, 1)); // 範囲内・一致
      insertNewsWithDate("旅行記", "D", LocalDate.of(2025, 4, 1)); // タイトル不一致（後で除外）
      insertNewsWithDate("Javaニュース", "E", LocalDate.of(2025, 5, 1)); // 範囲内・一致（最新）

      // Act:
      // タイトルに「Java」を含み、2025-02-01〜2025-05-31（両端含む*）の1ページ目・2件取得をリクエストする。
      NewsSearchRequest searchRequest =
          new NewsSearchRequest(1, 2, "Java", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 5, 31));
      NewsPageDto page = newsApplicationService.findNews(searchRequest.toCommand());

      // Assert
      assertEquals(2, page.totalSize());
      assertEquals(1, page.pageNum());
      assertEquals(2, page.pageSize());

      var items = page.newsDtos();
      assertEquals(2, items.size());
      assertEquals("Javaニュース", items.get(0).title());
      assertEquals("Java実践", items.get(1).title());
    }
  }

  @Nested
  class お知らせ更新 {
    @Test
    void お知らせを更新できる() {
      // Arrange: まず1件作成
      NewsCreateRequest createRequest = new NewsCreateRequest("初期タイトル", "初期本文");
      newsApplicationService.createNews(createRequest.toCommand());
      UUID id =
          jdbcTemplate.queryForObject("SELECT id FROM news WHERE title = ?", UUID.class, "初期タイトル");
      assertNotNull(id);

      // （任意）更新前の updated_at を保持して後で更新されたことも検証
      Timestamp beforeUpdatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM news WHERE id = ?", Timestamp.class, id);

      // Act: タイトルと本文を更新
      NewsUpdateRequest updateRequest = new NewsUpdateRequest(null, "更新後タイトル", "更新後本文");
      newsApplicationService.updateNews(updateRequest.toCommand(id));

      // Assert: DTO経由で内容が更新されていること
      NewsDto dto = newsApplicationService.findNewsById(id);
      assertEquals(id, dto.id());
      assertEquals("更新後タイトル", dto.title());
      assertEquals("更新後本文", dto.content());

      // DB上も行数が1件のままであること（上書き更新）
      Integer cnt =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news WHERE id = ?", Integer.class, id);
      assertEquals(1, cnt);

      // （任意）updated_at が更新されていること
      Timestamp afterUpdatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM news WHERE id = ?", Timestamp.class, id);
      assertNotNull(beforeUpdatedAt);
      assertNotNull(afterUpdatedAt);
      assertTrue(
          afterUpdatedAt.after(beforeUpdatedAt),
          () ->
              "updated_at が更新前と同一か過去になっています: before="
                  + beforeUpdatedAt
                  + ", after="
                  + afterUpdatedAt);
    }

    @Test
    void 存在しないIDを更新するとResourceNotFoundExceptionを投げる() {
      // Arrange
      UUID notExistsId = UUID.randomUUID();

      // Act & Assert
      NewsUpdateRequest request = new NewsUpdateRequest(null, "何か", "何か");
      assertThrows(
          RuntimeException.class,
          () -> newsApplicationService.updateNews(request.toCommand(notExistsId)));
    }
  }
}
