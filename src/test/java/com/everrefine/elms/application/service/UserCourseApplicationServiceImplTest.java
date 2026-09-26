package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.*;

import com.everrefine.elms.application.dto.UserCourseDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.math.BigDecimal;
import java.util.List;
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
public class UserCourseApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  @Autowired private TestDataFactory testData;

  @Autowired private UserCourseApplicationService userCourseApplicationService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  class コース一覧取得 {
    @Test
    void 進捗付きのコース一覧が返る() {
      // 1. usersテーブルに1ユーザー作る
      UUID userId = testData.createUser("ul-not-done@example.com", "p", "太郎", "ulnd", "GENERAL");
      // 2. coursesテーブルに2コース作る
      UUID courseId1 = testData.createCourse(new BigDecimal("1001"), "ULテストコース", "コース説明");
      UUID courseId2 = testData.createCourse(new BigDecimal("1002"), "ULテストコース2", "コース説明2");
      UUID lessonGroupId1 =
          testData.createLessonGroup(courseId1, new BigDecimal(1), "ULテストコースのグループ");
      UUID lessonGroupId2 =
          testData.createLessonGroup(courseId2, new BigDecimal(2), "ULテストコース2のグループ");

      // 3. lessonsテーブルレッスンを3個（1個と2個）作る
      UUID completedLessonId1 =
          testData.createLesson(
              lessonGroupId1,
              courseId1,
              new BigDecimal("1"),
              "UL完了レッスン1",
              "説明",
              "https://example.com/video.mp4");

      UUID completedLessonId2 =
          testData.createLesson(
              lessonGroupId2,
              courseId2,
              new BigDecimal("1"),
              "UL完了レッスン2",
              "説明",
              "https://example.com/video.mp4");

      testData.createLesson(
          lessonGroupId2,
          courseId2,
          new BigDecimal("2"),
          "UL未完了レッスン1",
          "説明",
          "https://example.com/video.mp4");
      // 4. userLessonsテーブルに2個（1個と1個）作る
      testData.createUserLesson(userId, completedLessonId1);
      testData.createUserLesson(userId, completedLessonId2);

      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);
      assertNotNull(result);
      UserCourseDto course1 =
          result.stream().filter(dto -> dto.title().equals("ULテストコース")).findFirst().orElseThrow();
      UserCourseDto course2 =
          result.stream().filter(dto -> dto.title().equals("ULテストコース2")).findFirst().orElseThrow();
      assertEquals(courseId1, course1.id());
      assertEquals(new BigDecimal("100.0"), course1.courseProgress());
      assertEquals(courseId2, course2.id());
      assertEquals(new BigDecimal("50.0"), course2.courseProgress());
    }

    @Test
    void コースが0件のとき空リストが返る() {
      jdbcTemplate.execute("DELETE FROM courses");

      UUID userId =
          testData.createUser("ul-put-mismatch@example.com", "p", "四郎", "ulpm", "GENERAL");
      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void 存在しないuserIdを指定した場合ResourceNotFoundExceptionを投げる() {
      assertThrows(
          ResourceNotFoundException.class,
          () -> userCourseApplicationService.findUserCourses(UUID.randomUUID()));
    }
  }
}
