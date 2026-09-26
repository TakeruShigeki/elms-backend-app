package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.everrefine.elms.application.command.LessonGroupCreateCommand;
import com.everrefine.elms.application.command.LessonGroupUpdateCommand;
import com.everrefine.elms.application.dto.LessonGroupDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.presentation.request.LessonGroupCreateRequest;
import com.everrefine.elms.presentation.request.LessonGroupUpdateRequest;
import java.math.BigDecimal;
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
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Testcontainers
@Transactional
class LessonGroupApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  @Autowired private LessonGroupApplicationServiceImpl lessonGroupApplicationService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  class レッスングループ作成 {
    @Test
    void レッスングループを作成できる() {
      // Arrange
      LocalDateTime now = LocalDateTime.now();
      jdbcTemplate.update(
          "INSERT INTO courses (course_order, title, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
          new BigDecimal("1"),
          "作成用コース",
          "説明",
          now,
          now);
      UUID courseId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM courses WHERE title = ?", UUID.class, "作成用コース");

      LessonGroupCreateRequest request = new LessonGroupCreateRequest("新しいグループ");
      LessonGroupCreateCommand command = request.toCommand(courseId);

      // Act
      LessonGroupDto result = lessonGroupApplicationService.createLessonGroup(command);

      // Assert
      assertNotNull(result.id());
      assertEquals(courseId, result.courseId());
      assertNotNull(result.lessonGroupOrder());
      assertEquals("新しいグループ", result.name());
      assertNotNull(result.createdAt());
      assertNotNull(result.updatedAt());
      assertNull(result.lessons());

      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_groups WHERE id = ?", Integer.class, result.id());
      assertEquals(1, count);
    }
  }

  @Nested
  class レッスングループ更新 {
    @Test
    void レッスングループを更新できる() {
      // Arrange
      LocalDateTime now = LocalDateTime.now();
      jdbcTemplate.update(
          "INSERT INTO courses (course_order, title, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
          new BigDecimal("1"),
          "テストコース",
          "コース説明",
          now,
          now);
      UUID courseId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM courses WHERE title = ?", UUID.class, "テストコース");

      jdbcTemplate.update(
          "INSERT INTO lesson_groups (course_id, lesson_group_order, title, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
          courseId,
          new BigDecimal("1"),
          "元のタイトル",
          now,
          now);
      UUID lessonGroupId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM lesson_groups WHERE title = ?", UUID.class, "元のタイトル");

      jdbcTemplate.update(
          "INSERT INTO lessons (lesson_group_id, course_id, lesson_order, title, content, video_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
          lessonGroupId,
          courseId,
          new BigDecimal("1"),
          "レッスン1",
          "説明1",
          "https://example.com/1.mp4",
          now,
          now);
      jdbcTemplate.update(
          "INSERT INTO lessons (lesson_group_id, course_id, lesson_order, title, content, video_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
          lessonGroupId,
          courseId,
          new BigDecimal("2"),
          "レッスン2",
          "説明2",
          "https://example.com/2.mp4",
          now,
          now);

      LessonGroupUpdateRequest request = new LessonGroupUpdateRequest("新しいタイトル");
      LessonGroupUpdateCommand command = request.toCommand(lessonGroupId);

      // Act
      LessonGroupDto result = lessonGroupApplicationService.updateLessonGroup(command);

      // Assert
      assertEquals(lessonGroupId, result.id());
      assertEquals(courseId, result.courseId());
      assertEquals(0, result.lessonGroupOrder().compareTo(new BigDecimal("1")));
      assertEquals("新しいタイトル", result.name());
      assertNotNull(result.createdAt());
      assertNotNull(result.updatedAt());
      assertNotNull(result.lessons());
      assertEquals(2, result.lessons().size());
      assertEquals("レッスン1", result.lessons().get(0).title());
      assertEquals("レッスン2", result.lessons().get(1).title());

      String updatedTitle =
          jdbcTemplate.queryForObject(
              "SELECT title FROM lesson_groups WHERE id = ?", String.class, lessonGroupId);
      assertEquals("新しいタイトル", updatedTitle);
    }

    @Test
    void 存在しないレッスングループIDの場合ResourceNotFoundExceptionを投げる() {
      // Arrange
      LessonGroupUpdateRequest request = new LessonGroupUpdateRequest("新しいタイトル");
      UUID nonExistentId = UUID.randomUUID();
      LessonGroupUpdateCommand command = request.toCommand(nonExistentId);

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonGroupApplicationService.updateLessonGroup(command));
      assertEquals("LessonGroup が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }
  }

  @Nested
  class レッスングループ削除 {
    @Test
    void レッスングループを削除できる() {
      // Arrange
      LocalDateTime now = LocalDateTime.now();
      jdbcTemplate.update(
          "INSERT INTO courses (course_order, title, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
          new BigDecimal("1"),
          "削除用コース",
          "説明",
          now,
          now);
      UUID courseId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM courses WHERE title = ?", UUID.class, "削除用コース");

      jdbcTemplate.update(
          "INSERT INTO lesson_groups (course_id, lesson_group_order, title, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
          courseId,
          new BigDecimal("1"),
          "削除対象グループ",
          now,
          now);
      UUID lessonGroupId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM lesson_groups WHERE title = ?", UUID.class, "削除対象グループ");

      // Act
      lessonGroupApplicationService.deleteLessonGroupById(lessonGroupId);

      // Assert
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_groups WHERE id = ?", Integer.class, lessonGroupId);
      assertEquals(0, count);
    }

    @Test
    void 存在しないレッスングループを削除してもエラーにならない() {
      // Arrange
      // Act
      lessonGroupApplicationService.deleteLessonGroupById(UUID.randomUUID());

      // Assert
    }
  }
}
