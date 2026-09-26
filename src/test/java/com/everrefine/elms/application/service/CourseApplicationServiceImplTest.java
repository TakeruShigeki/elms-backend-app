package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.dto.CourseDto;
import com.everrefine.elms.application.dto.CoursePageDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.presentation.request.CourseCreateRequest;
import com.everrefine.elms.presentation.request.CourseUpdateRequest;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
public class CourseApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  @Autowired private TestDataFactory testData;

  @Autowired private CourseApplicationServiceImpl courseApplicationService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void deleteCourses() {
    jdbcTemplate.execute("DELETE FROM courses");
  }

  // タイトルからidを取得する
  private UUID getIdByTitle(String title) {
    UUID id =
        jdbcTemplate.queryForObject("SELECT id FROM courses WHERE title = ?", UUID.class, title);
    assertNotNull(id);
    return id;
  }

  @Nested
  class コース作成 {
    @Test
    void コースを新規作成できる() {
      // Arrange
      CourseCreateRequest request =
          new CourseCreateRequest("テストタイトル", "テスト説明文", "https://test.com/xxx");

      // Act
      courseApplicationService.createCourse(request.toCommand());

      // Assert
      UUID id = getIdByTitle("テストタイトル");
      CourseDto dto = courseApplicationService.findCourseById(id);
      assertEquals(id, dto.id());
      assertEquals("テストタイトル", dto.title());
      assertEquals("テスト説明文", dto.description());
      assertEquals("https://test.com/xxx", dto.thumbnailUrl());
    }
  }

  @Nested
  class コース取得 {
    @Test
    void ページング昇順で取得できる() {
      // Arrange
      testData.createCourse(new BigDecimal("1024"), "Java入門", "a", "https://test.com/xxx");
      testData.createCourse(new BigDecimal("2048"), "Spring解説", "b", "https://test.com/yyy");
      testData.createCourse(new BigDecimal("3072"), "Java実践", "c", "https://test.com/zzz");

      // Act
      CoursePageDto page = courseApplicationService.findCourses(1, 2);

      // Assert
      assertEquals(3, page.totalSize());
      assertEquals(1, page.pageNum());
      assertEquals(2, page.pageSize());

      var items = page.courseDtos();
      assertEquals(2, items.size());
      assertEquals("Java入門", items.get(0).title());
      assertEquals("Spring解説", items.get(1).title());
    }
  }

  @Nested
  class コース更新 {
    @Test
    void コースを更新できる() {
      // Arrange
      UUID id =
          testData.createCourse(
              new BigDecimal("1024"), "テストタイトル", "テスト説明文", "https://test.com/xxx");

      Timestamp beforeUpdatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM courses WHERE id = ?", Timestamp.class, id);

      // Act
      CourseUpdateRequest updateRequest =
          new CourseUpdateRequest(
              null, new BigDecimal("1.0000"), "更新後タイトル", "更新後説明文", "https://test.com/xxx");
      courseApplicationService.updateCourse(updateRequest.toCommand(id));

      // Assert
      CourseDto dto = courseApplicationService.findCourseById(id);
      assertEquals(id, dto.id());
      assertEquals("更新後説明文", dto.description());
      assertEquals("更新後タイトル", dto.title());
      assertEquals("https://test.com/xxx", dto.thumbnailUrl());
      assertEquals(new BigDecimal("1.0000"), dto.courseOrder());

      Integer cnt =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM courses WHERE id = ?", Integer.class, id);
      assertEquals(1, cnt);

      Timestamp afterUpdatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM courses WHERE id = ?", Timestamp.class, id);
      assertNotNull(beforeUpdatedAt);
      assertNotNull(afterUpdatedAt);
      assertTrue(
          afterUpdatedAt.after(beforeUpdatedAt),
          () ->
              "updated_atが更新前と同一か過去になっています: before="
                  + beforeUpdatedAt
                  + ", after="
                  + afterUpdatedAt);
    }

    @Test
    void 存在しないIDを更新するとResourceNotFoundExceptionを投げる() {
      // Arrange
      UUID notExistsId = UUID.randomUUID();

      // Act & Assert
      CourseUpdateRequest updateRequest =
          new CourseUpdateRequest(
              null, new BigDecimal("1.0000"), "更新後タイトル", "更新後説明文", "https://test.com/xxx");
      assertThrows(
          ResourceNotFoundException.class,
          () -> courseApplicationService.updateCourse(updateRequest.toCommand(notExistsId)));
    }

    @Test
    void 表示順が他のコースと重複するとBadRequestExceptionを投げる() {
      // Arrange
      testData.createCourse(new BigDecimal("1024"), "既存コース", "既存説明文");
      UUID id = testData.createCourse(new BigDecimal("2048"), "更新対象コース", "更新対象説明文");

      // Act & Assert
      CourseUpdateRequest updateRequest =
          new CourseUpdateRequest(null, new BigDecimal("1024"), "更新後タイトル", "更新後説明文", null);
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> courseApplicationService.updateCourse(updateRequest.toCommand(id)));
      assertEquals("この表示順はすでに使用されています", exception.getMessage());
    }

    @Test
    void 表示順を変更せずに更新できる() {
      // Arrange
      UUID id = testData.createCourse(new BigDecimal("1024"), "更新対象コース", "更新対象説明文");

      // Act
      CourseUpdateRequest updateRequest =
          new CourseUpdateRequest(null, new BigDecimal("1024"), "更新後タイトル", "更新後説明文", null);
      courseApplicationService.updateCourse(updateRequest.toCommand(id));

      // Assert
      CourseDto dto = courseApplicationService.findCourseById(id);
      assertEquals("更新後タイトル", dto.title());
      assertEquals(new BigDecimal("1024.0000"), dto.courseOrder());
    }
  }

  @Nested
  class コース削除 {
    @Test
    void コースを削除できる() {
      // Arrange
      UUID id =
          testData.createCourse(
              new BigDecimal("1024"), "テストタイトル", "テスト説明文", "https://test.com/xxx");

      // Act
      courseApplicationService.deleteCourseById(id);

      // Assert
      assertThrows(
          ResourceNotFoundException.class, () -> courseApplicationService.findCourseById(id));
    }
  }
}
