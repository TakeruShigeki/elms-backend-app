package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.command.LessonCreateCommand;
import com.everrefine.elms.application.command.LessonImportCommand;
import com.everrefine.elms.application.command.LessonOrderUpdateCommand;
import com.everrefine.elms.application.command.LessonSearchCommand;
import com.everrefine.elms.application.command.LessonUpdateCommand;
import com.everrefine.elms.application.dto.CourseLessonsDto;
import com.everrefine.elms.application.dto.LessonDto;
import com.everrefine.elms.application.dto.LessonImportResponseDto;
import com.everrefine.elms.application.dto.LessonPageDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.presentation.request.LessonCreateRequest;
import com.everrefine.elms.presentation.request.LessonOrderUpdateRequest;
import com.everrefine.elms.presentation.request.LessonSearchRequest;
import com.everrefine.elms.presentation.request.LessonUpdateRequest;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * {@link LessonApplicationServiceImpl}の統合テストクラス。
 *
 * <p>このテストクラスでは、実際のPostgreSQLデータベースを使用した統合テストを実施します。 Testcontainersを利用してテスト用のDockerコンテナを起動し、Spring
 * Bootのテスト機能と組み合わせることで、 実運用環境に近い状態でのテストを保証します。
 *
 * <p>主な検証項目：
 *
 * <ul>
 *   <li>実運用に近いシナリオの網羅
 *   <li>エッジケースと例外処理の検証
 *   <li>実際のデータベース操作の検証
 *   <li>ビジネスロジックの正確性
 *   <li>トランザクション境界の考慮
 * </ul>
 */
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE) // WebまわりのConfigurationはBean生成を無効にして高速化する。
@Testcontainers // DBはDockerコンテナを使用する。
@Transactional // 各テストメソッド終了時にテストデータをロールバックする。
public class LessonApplicationServiceImplTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  /** テスト対象のサービスクラス。 */
  @Autowired private TestDataFactory testData;

  @Autowired private LessonApplicationServiceImpl lessonApplicationService;

  /** レッスンリポジトリ。 */
  @Autowired private LessonRepository lessonRepository;

  /** データ検証で使用するためのJdbcTemplate。 */
  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  class レッスン取得 {
    @Test
    void レッスンをIDで取得できる() {
      // Arrange - テストデータを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "テストレッスン",
              "テスト説明",
              "https://example.com/video.mp4");

      // Act
      LessonDto result = lessonApplicationService.findLessonById(courseId, lessonGroupId, lessonId);

      // Assert
      assertNotNull(result);
      assertEquals(lessonId, result.id());
      assertEquals(lessonGroupId, result.lessonGroupId());
      assertEquals(courseId, result.courseId());
      assertEquals(new BigDecimal("1.0000"), result.lessonOrder());
      assertEquals("テストレッスン", result.title());
      assertEquals("テスト説明", result.content());
      assertEquals("https://example.com/video.mp4", result.videoUrl());
      assertNotNull(result.createdAt());
      assertNotNull(result.updatedAt());
    }

    @Test
    void 存在しないレッスンIDでResourceNotFoundExceptionを投げる() {
      // Act & Assert
      UUID nonExistentId = UUID.randomUUID();
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () ->
                  lessonApplicationService.findLessonById(
                      UUID.randomUUID(), UUID.randomUUID(), nonExistentId));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }

    @Test
    void パスのコースまたはグループがレッスンと一致しない場合ResourceNotFoundExceptionを投げる() {
      UUID courseId = testData.createCourse(new BigDecimal("1"), "整合コース", "説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "整合グループ");
      UUID lessonId =
          testData.createLesson(lessonGroupId, courseId, new BigDecimal("1"), "L1", "d", null);
      UUID otherCourseId = testData.createCourse(new BigDecimal("2"), "別コース", "説明");

      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () ->
                  lessonApplicationService.findLessonById(otherCourseId, lessonGroupId, lessonId));
      assertEquals("Lesson が見つかりませんでした。id = " + lessonId, exception.getMessage());
    }
  }

  @Nested
  class レッスン検索 {
    @Test
    void レッスンを検索できる() {
      // Arrange - テストデータを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      testData.createLesson(
          lessonGroupId,
          courseId,
          new BigDecimal("1"),
          "テストレッスン",
          "テスト説明",
          "https://example.com/video.mp4");

      LessonSearchRequest searchRequest =
          new LessonSearchRequest(1, 10, String.valueOf(courseId), null, null, null, null);
      LessonSearchCommand searchCommand = searchRequest.toCommand();

      // Act
      LessonPageDto result = lessonApplicationService.findLessons(searchCommand);

      // Assert
      assertNotNull(result);
      assertTrue(result.totalSize() >= 1);
      assertEquals(1, result.pageNum());
      assertEquals(10, result.pageSize());
    }

    @Test
    void 検索結果が0件のとき空リストが返る() {
      // Arrange - データが存在しない状態
      LessonSearchRequest searchRequest =
          new LessonSearchRequest(1, 10, UUID.randomUUID().toString(), null, null, null, null);
      LessonSearchCommand searchCommand = searchRequest.toCommand();

      // Act
      LessonPageDto result = lessonApplicationService.findLessons(searchCommand);

      // Assert
      assertNotNull(result);
      assertEquals(0, result.totalSize());
      assertEquals(1, result.pageNum());
      assertEquals(10, result.pageSize());
      assertTrue(result.lessonDtos().isEmpty());
    }
  }

  @Nested
  class コース別レッスン一覧取得 {
    @Test
    void コース別レッスン一覧を取得できる() {
      // Arrange - テストデータを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // Act
      CourseLessonsDto result = lessonApplicationService.findLessonsGroupedByLessonGroup(courseId);

      // Assert
      assertNotNull(result);
      assertNotNull(result.lessonGroups());
    }
  }

  @Nested
  class レッスン作成 {
    @Test
    void レッスンを作成できる() {
      // Arrange - 関連データを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      LessonCreateRequest request =
          new LessonCreateRequest("新規レッスン", "新規説明", "https://example.com/new-video.mp4");
      LessonCreateCommand command = request.toCommand(courseId, lessonGroupId);

      // Act
      LessonDto result = lessonApplicationService.createLesson(command);

      // Assert
      assertNotNull(result);
      assertEquals("新規レッスン", result.title());
      assertEquals("新規説明", result.content());
      assertEquals("https://example.com/new-video.mp4", result.videoUrl());

      // DBにデータが保存されていることを確認
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE title = ?", Integer.class, "新規レッスン");
      assertEquals(1, count);
      String savedContent =
          jdbcTemplate.queryForObject(
              "SELECT content FROM lessons WHERE title = ?", String.class, "新規レッスン");
      assertEquals("新規説明", savedContent);
    }

    @Test
    void null許容フィールドを指定せずにレッスンを作成できる() {
      // Arrange - 関連データを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      LessonCreateRequest request = new LessonCreateRequest("最小構成レッスン", null, null);
      LessonCreateCommand command = request.toCommand(courseId, lessonGroupId);

      // Act
      LessonDto result = lessonApplicationService.createLesson(command);

      // Assert
      assertNotNull(result);
      assertEquals("最小構成レッスン", result.title());
      assertNull(result.content());
      assertNull(result.videoUrl());
    }

    @Test
    void 複数レッスンを一括作成できる() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "一括登録コース", "コース説明");
      UUID lessonGroupId1 =
          testData.createLessonGroup(courseId, new BigDecimal("1024"), "一括登録グループ1");
      UUID lessonGroupId2 =
          testData.createLessonGroup(courseId, new BigDecimal("2048"), "一括登録グループ2");

      List<Lesson> lessons =
          List.of(
              Lesson.create(
                  lessonGroupId1,
                  courseId,
                  new BigDecimal("1024"),
                  "一括登録レッスン1",
                  "説明1",
                  "https://example.com/batch1.mp4"),
              Lesson.create(
                  lessonGroupId1, courseId, new BigDecimal("2048"), "一括登録レッスン2", null, null),
              Lesson.create(
                  lessonGroupId2, courseId, new BigDecimal("1024"), "一括登録レッスン3", "説明3", null));

      // Act
      lessonRepository.createLessons(lessons);
      lessonRepository.createLessons(List.of());

      // Assert
      List<Map<String, Object>> actualLessons =
          jdbcTemplate.queryForList(
              """
              SELECT l.lesson_group_id, l.course_id, l.lesson_order, l.title, l.content, l.video_url
              FROM lessons l
              JOIN lesson_groups g ON g.id = l.lesson_group_id
              WHERE l.course_id = ?
              ORDER BY g.lesson_group_order ASC, l.lesson_order ASC
              """,
              courseId);

      assertEquals(3, actualLessons.size());
      assertEquals(lessonGroupId1, actualLessons.get(0).get("lesson_group_id"));
      assertEquals(courseId, actualLessons.get(0).get("course_id"));
      assertEquals(new BigDecimal("1024.0000"), actualLessons.get(0).get("lesson_order"));
      assertEquals("一括登録レッスン1", actualLessons.get(0).get("title"));
      assertEquals("説明1", actualLessons.get(0).get("content"));
      assertEquals("https://example.com/batch1.mp4", actualLessons.get(0).get("video_url"));

      assertEquals(lessonGroupId1, actualLessons.get(1).get("lesson_group_id"));
      assertEquals(new BigDecimal("2048.0000"), actualLessons.get(1).get("lesson_order"));
      assertEquals("一括登録レッスン2", actualLessons.get(1).get("title"));
      assertNull(actualLessons.get(1).get("content"));
      assertNull(actualLessons.get(1).get("video_url"));

      assertEquals(lessonGroupId2, actualLessons.get(2).get("lesson_group_id"));
      assertEquals(new BigDecimal("1024.0000"), actualLessons.get(2).get("lesson_order"));
      assertEquals("一括登録レッスン3", actualLessons.get(2).get("title"));
      assertEquals("説明3", actualLessons.get(2).get("content"));
      assertNull(actualLessons.get(2).get("video_url"));
    }
  }

  @Nested
  class レッスンCSV取込 {
    @Test
    void 既存レッスン構成を置き換えられる() throws Exception {
      // Arrange - 既存のレッスン構成を準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "CSV取込コース", "コース説明");
      UUID oldLessonGroupId =
          testData.createLessonGroup(courseId, new BigDecimal("1024"), "削除対象グループ");
      testData.createLesson(
          oldLessonGroupId,
          courseId,
          new BigDecimal("1024"),
          "削除対象レッスン",
          "削除対象説明",
          "https://example.com/old.mp4");

      String csv =
          String.join(
              "\n",
              "レッスングループタイトル,レッスンタイトル,レッスン説明,レッスンの動画URL",
              "Basic,Lesson 1,説明1,https://example.com/1.mp4",
              "Advanced,Lesson 2,,",
              "Basic,Lesson 3,,https://example.com/3.mp4");
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "lessons.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

      // Act
      LessonImportResponseDto result =
          lessonApplicationService.importLessonsCsv(
              LessonImportCommand.from(courseId, file.getOriginalFilename(), file.getBytes()));

      // Assert - 取込件数
      assertEquals(2, result.importedLessonGroupCount());
      assertEquals(3, result.importedLessonCount());

      // Assert - 既存構成は削除されている
      Integer oldLessonCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE title = ?", Integer.class, "削除対象レッスン");
      assertEquals(0, oldLessonCount);
      Integer oldLessonGroupCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_groups WHERE title = ?", Integer.class, "削除対象グループ");
      assertEquals(0, oldLessonGroupCount);

      // Assert - レッスングループは初出順で1024間隔になる
      List<Map<String, Object>> lessonGroups =
          jdbcTemplate.queryForList(
              """
              SELECT id, title, lesson_group_order
              FROM lesson_groups
              WHERE course_id = ?
              ORDER BY lesson_group_order ASC
              """,
              courseId);
      assertEquals(2, lessonGroups.size());
      assertEquals("Basic", lessonGroups.get(0).get("title"));
      assertEquals(new BigDecimal("1024.0000"), lessonGroups.get(0).get("lesson_group_order"));
      assertEquals("Advanced", lessonGroups.get(1).get("title"));
      assertEquals(new BigDecimal("2048.0000"), lessonGroups.get(1).get("lesson_group_order"));

      // Assert - 同じグループタイトルの行は同一グループにまとまり、レッスン順はグループ内のCSV順になる
      UUID basicLessonGroupId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM lesson_groups WHERE course_id = ? AND title = ?",
              UUID.class,
              courseId,
              "Basic");
      List<Map<String, Object>> basicLessons =
          jdbcTemplate.queryForList(
              """
              SELECT title, lesson_order, content, video_url
              FROM lessons
              WHERE course_id = ? AND lesson_group_id = ?
              ORDER BY lesson_order ASC
              """,
              courseId,
              basicLessonGroupId);
      assertEquals(2, basicLessons.size());
      assertEquals("Lesson 1", basicLessons.get(0).get("title"));
      assertEquals(new BigDecimal("1024.0000"), basicLessons.get(0).get("lesson_order"));
      assertEquals("説明1", basicLessons.get(0).get("content"));
      assertEquals("https://example.com/1.mp4", basicLessons.get(0).get("video_url"));
      assertEquals("Lesson 3", basicLessons.get(1).get("title"));
      assertEquals(new BigDecimal("2048.0000"), basicLessons.get(1).get("lesson_order"));
      assertNull(basicLessons.get(1).get("content"));
      assertEquals("https://example.com/3.mp4", basicLessons.get(1).get("video_url"));

      UUID advancedLessonGroupId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM lesson_groups WHERE course_id = ? AND title = ?",
              UUID.class,
              courseId,
              "Advanced");
      Map<String, Object> advancedLesson =
          jdbcTemplate.queryForMap(
              """
              SELECT title, lesson_order, content, video_url
              FROM lessons
              WHERE course_id = ? AND lesson_group_id = ?
              """,
              courseId,
              advancedLessonGroupId);
      assertEquals("Lesson 2", advancedLesson.get("title"));
      assertEquals(new BigDecimal("1024.0000"), advancedLesson.get("lesson_order"));
      assertNull(advancedLesson.get("content"));
      assertNull(advancedLesson.get("video_url"));
    }

    @Test
    void 存在しないコースを指定した場合ResourceNotFoundExceptionを投げ既存構成が削除されない() throws Exception {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "CSV取込失敗コース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1024"), "保持対象グループ");
      testData.createLesson(
          lessonGroupId,
          courseId,
          new BigDecimal("1024"),
          "保持対象レッスン",
          "保持対象説明",
          "https://example.com/keep.mp4");

      String csv =
          String.join(
              "\n",
              "レッスングループタイトル,レッスンタイトル,レッスン説明,レッスンの動画URL",
              "Basic,Lesson 1,説明1,https://example.com/1.mp4");
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "lessons.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
      LessonImportCommand command =
          LessonImportCommand.from(UUID.randomUUID(), file.getOriginalFilename(), file.getBytes());

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> lessonApplicationService.importLessonsCsv(command));

      Integer lessonGroupCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_groups WHERE course_id = ?", Integer.class, courseId);
      assertEquals(1, lessonGroupCount);
      Integer lessonCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE course_id = ?", Integer.class, courseId);
      assertEquals(1, lessonCount);
    }
  }

  @Nested
  class レッスン更新 {
    @Test
    void レッスンを更新できる() {
      // Arrange - 既存レッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");

      LessonUpdateRequest request =
          new LessonUpdateRequest("更新後タイトル", "更新後説明", "https://example.com/updated-video.mp4");
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert
      assertNotNull(result);
      assertEquals(lessonId, result.id());
      assertEquals("更新後タイトル", result.title());
      assertEquals("更新後説明", result.content());
      assertEquals("https://example.com/updated-video.mp4", result.videoUrl());

      // DBが更新されていることを確認
      String updatedTitle =
          jdbcTemplate.queryForObject(
              "SELECT title FROM lessons WHERE id = ?", String.class, lessonId);
      assertEquals("更新後タイトル", updatedTitle);
      String updatedContent =
          jdbcTemplate.queryForObject(
              "SELECT content FROM lessons WHERE id = ?", String.class, lessonId);
      assertEquals("更新後説明", updatedContent);
    }

    @Test
    void null許容フィールドをnullで更新できる() {
      // Arrange - 既存レッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");

      // nullを渡すと元の値が保持される仕様
      LessonUpdateRequest request = new LessonUpdateRequest("タイトルのみ更新", null, null);
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert - nullを渡した場合は元の値が保持される
      assertNotNull(result);
      assertEquals("タイトルのみ更新", result.title());
      assertEquals("元の説明", result.content()); // 元の値が保持される
      assertEquals("https://example.com/old-video.mp4", result.videoUrl()); // 元の値が保持される
    }

    @Test
    void 存在しないレッスンを更新するとResourceNotFoundExceptionを投げる() {
      // Arrange
      LessonUpdateRequest request =
          new LessonUpdateRequest("存在しないレッスン", "説明", "https://example.com/video.mp4");
      UUID nonExistentId = UUID.randomUUID();
      LessonUpdateCommand command = request.toCommand(nonExistentId);

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLesson(command));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }
  }

  @Nested
  class レッスン削除 {
    @Test
    void 存在するレッスンを削除できる() {
      // Arrange - 削除対象のレッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "削除対象レッスン",
              "説明",
              "https://example.com/video.mp4");

      // Act
      lessonApplicationService.deleteLessonById(lessonId);

      // Assert - レッスンが削除されていることを確認
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE id = ?", Integer.class, lessonId);
      assertEquals(0, count);
    }

    @Test
    void 存在しないレッスンを削除してもエラーにならない() {
      // Act - 存在しないIDで削除を実行
      lessonApplicationService.deleteLessonById(UUID.randomUUID());

      // Assert - 例外が発生しないことを確認（このテストが成功すればOK）
    }

    @Test
    void 複数回の削除操作が安全に実行できる() {
      // Arrange - 削除対象のレッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "複数回削除テスト",
              "説明",
              "https://example.com/video.mp4");

      // Act
      lessonApplicationService.deleteLessonById(lessonId);
      lessonApplicationService.deleteLessonById(lessonId); // 2回目

      // Assert - 2回目の削除もエラーにならないことを確認
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE id = ?", Integer.class, lessonId);
      assertEquals(0, count);
    }
  }

  @Nested
  class レッスン並び替えの境界 {

    /** 隣接する2件の間隔が小数第4位を下回ると中間値を作れないため、500ではなく400で弾く。 */
    @Test
    void 間隔が狭すぎて移動できない場合はBadRequestExceptionを投げる() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 並び順の差が最小単位（0.0001）しかない2件を用意する
      UUID precedingLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1000.0000"), "レッスン1", "説明1", null);
      UUID followingLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1000.0001"), "レッスン2", "説明2", null);
      UUID targetLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("2000.0000"), "レッスン3", "説明3", null);

      // Act & Assert
      LessonOrderUpdateRequest request =
          new LessonOrderUpdateRequest(precedingLessonId, followingLessonId);
      LessonOrderUpdateCommand command = request.toCommand(targetLessonId);
      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> lessonApplicationService.updateLessonOrder(command));
      assertEquals("この位置にはこれ以上移動できません。前後のレッスンの並び順を空けてから再度実行してください", exception.getMessage());

      // 元の並び順が保持されていること
      BigDecimal order =
          jdbcTemplate.queryForObject(
              "SELECT lesson_order FROM lessons WHERE id = ?", BigDecimal.class, targetLessonId);
      assertEquals(new BigDecimal("2000.0000"), order);
    }

    @Test
    void 前後のレッスンをどちらも指定しない場合はBadRequestExceptionを投げる() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID targetLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1000.0000"), "レッスン1", "説明1", null);

      // Act & Assert
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(null, null);
      LessonOrderUpdateCommand command = request.toCommand(targetLessonId);
      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> lessonApplicationService.updateLessonOrder(command));
      assertEquals("移動先の前後いずれかのレッスンを指定してください", exception.getMessage());
    }
  }

  @Nested
  class レッスン並び替え {
    @Test
    void 指定した2つのレッスンの間に移動できる() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 3つのレッスンを作成（order: 1000, 2000, 3000）
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      UUID lesson2Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("2000"),
              "レッスン2",
              "説明2",
              "https://example.com/video2.mp4");

      UUID lesson3Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("3000"),
              "レッスン3",
              "説明3",
              "https://example.com/video3.mp4");

      // Act - レッスン3をレッスン1と2の間に移動
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(lesson1Id, lesson2Id);
      LessonOrderUpdateCommand command = request.toCommand(lesson3Id);
      LessonDto result = lessonApplicationService.updateLessonOrder(command);

      // Assert - 新しい順序は (1000 + 2000) / 2 = 1500
      assertNotNull(result);
      assertEquals(lesson3Id, result.id());
      assertEquals(new BigDecimal("1500.0000"), result.lessonOrder());

      // DBが更新されていることを確認
      BigDecimal updatedOrder =
          jdbcTemplate.queryForObject(
              "SELECT lesson_order FROM lessons WHERE id = ?", BigDecimal.class, lesson3Id);
      assertEquals(new BigDecimal("1500.0000"), updatedOrder);
    }

    @Test
    void 先頭に移動できる() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 2つのレッスンを作成
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");
      UUID lesson2Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("2000"),
              "レッスン2",
              "説明2",
              "https://example.com/video2.mp4");

      // Act - レッスン2を先頭に移動（precedingLessonIdをnullに）
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(null, lesson1Id);
      LessonOrderUpdateCommand command = request.toCommand(lesson2Id);
      LessonDto result = lessonApplicationService.updateLessonOrder(command);

      // Assert - 新しい順序は 1000 / 2 = 500
      assertNotNull(result);
      assertEquals(lesson2Id, result.id());
      assertEquals(new BigDecimal("500.0000"), result.lessonOrder());

      // DBが更新されていることを確認
      BigDecimal updatedOrder =
          jdbcTemplate.queryForObject(
              "SELECT lesson_order FROM lessons WHERE id = ?", BigDecimal.class, lesson2Id);
      assertEquals(new BigDecimal("500.0000"), updatedOrder);
    }

    @Test
    void 末尾に移動できる() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 2つのレッスンを作成
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");
      UUID lesson2Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("2000"),
              "レッスン2",
              "説明2",
              "https://example.com/video2.mp4");

      // Act - レッスン1を末尾に移動（followingLessonIdをnullに）
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(lesson2Id, null);
      LessonOrderUpdateCommand command = request.toCommand(lesson1Id);
      LessonDto result = lessonApplicationService.updateLessonOrder(command);

      // Assert - 新しい順序は 2000 + 1024 = 3024
      assertNotNull(result);
      assertEquals(lesson1Id, result.id());
      assertEquals(new BigDecimal("3024.0000"), result.lessonOrder());

      // DBが更新されていることを確認
      BigDecimal updatedOrder =
          jdbcTemplate.queryForObject(
              "SELECT lesson_order FROM lessons WHERE id = ?", BigDecimal.class, lesson1Id);
      assertEquals(new BigDecimal("3024.0000"), updatedOrder);
    }

    @Test
    void 存在しないレッスンIDで並び替えするとResourceNotFoundExceptionを投げる() {
      // Arrange - 存在するレッスンを1つ準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      // Act & Assert - 存在しないレッスンIDで並び替え
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(lesson1Id, null);
      UUID nonExistentId = UUID.randomUUID();
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLessonOrder(request.toCommand(nonExistentId)));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }

    @Test
    void 存在しないprecedingLessonIdでResourceNotFoundExceptionを投げる() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      // Act & Assert - 存在しないprecedingLessonId
      UUID nonExistentId = UUID.randomUUID();
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(nonExistentId, null);
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLessonOrder(request.toCommand(lesson1Id)));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }

    @Test
    void 存在しないfollowingLessonIdでResourceNotFoundExceptionを投げる() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      // Act & Assert - 存在しないfollowingLessonId
      UUID nonExistentId = UUID.randomUUID();
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(null, nonExistentId);
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLessonOrder(request.toCommand(lesson1Id)));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }
  }
}
