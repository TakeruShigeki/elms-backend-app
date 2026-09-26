package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.command.LoginHistoryCreateCommand;
import com.everrefine.elms.application.command.PasswordUpdateCommand;
import com.everrefine.elms.application.command.UserImportCommand;
import com.everrefine.elms.application.command.UserSearchCommand;
import com.everrefine.elms.application.dto.UserImportResponseDto;
import com.everrefine.elms.application.dto.UserPageDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.repository.UserRepository;
import com.everrefine.elms.presentation.request.PasswordUpdateRequest;
import com.everrefine.elms.presentation.request.UserCreateRequest;
import com.everrefine.elms.presentation.request.UserUpdateRequest;
import com.everrefine.elms.testsupport.TestDataFactory;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Testcontainers
@Transactional
class UserApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  @Autowired private TestDataFactory testData;

  @Autowired private UserApplicationServiceImpl userApplicationService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  void deleteUsers() {
    jdbcTemplate.execute("DELETE FROM users");
  }

  /** テスト間で認証情報が漏れないようにクリアする。 */
  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  // 認証済みユーザーを作成
  public void createAuthentication(User user) {
    createAuthentication(user.id(), user.password().value());
  }

  // 指定したユーザーIDで認証済みにする（DBに存在しないIDも指定できる）
  public void createAuthentication(UUID userId, String password) {
    UserDetails userDetails =
        new org.springframework.security.core.userdetails.User(
            userId.toString(), password, List.of());
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  // 最終ログイン履歴を作成
  public void createLastLoginHistory(UUID userId, LocalDateTime dateTime) {
    jdbcTemplate.update(
        "INSERT INTO user_login_histories (user_id, created_at, updated_at) VALUES (?, ?, ?)",
        userId,
        dateTime,
        dateTime);
  }

  @WithMockUser(username = "00000000-0000-0000-0000-000000000000")
  @Nested
  class ユーザーCSV出力 {
    @Test
    void ユーザー情報がCSV形式で生成される() throws IOException, CsvValidationException {

      // Arrange
      LocalDateTime dateTime1 = LocalDateTime.of(2026, 3, 21, 9, 30);
      LocalDateTime dateTime2 = LocalDateTime.of(2026, 3, 21, 9, 31);
      UUID userId1 =
          testData.createUser(
              "test1@example.com", "password", "テスト はじめ", "test1\nuser", "GENERAL", dateTime1);
      UUID userId2 =
          testData.createUser(
              "test2@example.com", "password", "テスト\"\" 次郎", "test2,user", "GENERAL", dateTime2);
      createLastLoginHistory(userId1, dateTime1);
      createLastLoginHistory(userId2, dateTime2);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
      String dateTimeFormat1 = dateTime1.format(formatter);
      String dateTimeFormat2 = dateTime2.format(formatter);
      // Act
      Resource usersCsv = userApplicationService.exportUsersCsv();

      // Assert
      CSVReader csvReader =
          new CSVReader(new InputStreamReader(usersCsv.getInputStream(), StandardCharsets.UTF_8));

      String[] header = csvReader.readNext();
      if (header[0].startsWith("\uFEFF")) {
        header[0] = header[0].substring(1);
      }

      assertNotNull(header);
      assertArrayEquals(
          new String[] {"ユーザーID", "権限", "氏名", "メールアドレス", "ユーザー名", "作成日時", "最終ログイン日時", "進捗率"},
          header);
      String[] row1 = csvReader.readNext();
      assertArrayEquals(
          new String[] {
            String.valueOf(userId1),
            "一般",
            "テスト はじめ",
            "test1@example.com",
            "test1\nuser",
            dateTimeFormat1,
            dateTimeFormat1,
            "0.0%"
          },
          row1);
      String[] row2 = csvReader.readNext();
      assertArrayEquals(
          new String[] {
            String.valueOf(userId2),
            "一般",
            "テスト\"\" 次郎",
            "test2@example.com",
            "test2,user",
            dateTimeFormat2,
            dateTimeFormat2,
            "0.0%"
          },
          row2);
    }
  }

  @Nested
  class パスワード更新 {
    @Test
    void パスワードと更新日時が更新される() {
      // Arrange
      LocalDateTime dateTime = LocalDateTime.of(2026, 3, 21, 9, 30);
      UUID userId =
          testData.createUser(
              "test@example.com", "currentPass", "テスト はじめ", "tester", "GENERAL", dateTime);
      User user = userRepository.findUserById(userId).orElseThrow();
      createAuthentication(user);

      PasswordUpdateRequest request = new PasswordUpdateRequest("currentPass", "newPass");
      PasswordUpdateCommand passwordUpdateCommand = request.toCommand();
      PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      // Act
      userApplicationService.updatePassword(passwordUpdateCommand);
      User updatedUser = userRepository.findUserById(userId).orElseThrow();

      // Assert
      assertTrue(passwordEncoder.matches("newPass", updatedUser.password().value()));
      assertNotEquals(updatedUser.updatedAt(), dateTime);
      assertEquals("tester", updatedUser.userName().value());
    }

    @Test
    void 認証情報がないとき401のResponseStatusExceptionを投げる() {
      // Arrange
      LocalDateTime dateTime = LocalDateTime.of(2026, 3, 21, 9, 30);
      UUID userId =
          testData.createUser(
              "test@example.com", "currentPass", "テスト はじめ", "tester", "GENERAL", dateTime);
      User user = userRepository.findUserById(userId).orElseThrow();
      PasswordUpdateRequest request = new PasswordUpdateRequest("currentPass", "newPass");
      PasswordUpdateCommand passwordUpdateCommand = request.toCommand();
      // Act & Assert
      ResponseStatusException ex =
          assertThrows(
              ResponseStatusException.class,
              () -> userApplicationService.updatePassword(passwordUpdateCommand));
      assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void ユーザーが存在しないときResourceNotFoundExceptionを投げる() {
      // Arrange - DBに存在しないユーザーIDで認証済みの状態にする
      createAuthentication(UUID.randomUUID(), "currentPass");
      PasswordUpdateRequest request = new PasswordUpdateRequest("currentPass", "newPass");
      PasswordUpdateCommand passwordUpdateCommand = request.toCommand();

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> userApplicationService.updatePassword(passwordUpdateCommand));
    }

    @Test
    void パスワードが一致しないとき400のResponseStatusExceptionを投げる() {
      // Arrange
      LocalDateTime dateTime = LocalDateTime.of(2026, 3, 21, 9, 30);
      UUID userId =
          testData.createUser(
              "test@example.com", "currentPass", "テスト はじめ", "tester", "GENERAL", dateTime);
      User user = userRepository.findUserById(userId).orElseThrow();
      createAuthentication(user);

      PasswordUpdateRequest request = new PasswordUpdateRequest("wrongPass", "newPass");
      PasswordUpdateCommand passwordUpdateCommand = request.toCommand();

      // Act & Assert
      ResponseStatusException ex =
          assertThrows(
              ResponseStatusException.class,
              () -> userApplicationService.updatePassword(passwordUpdateCommand));
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
  }

  @Nested
  class ユーザーCSV取込 {
    @Test
    void CSVファイルをアップロードしてユーザー情報が洗い替えされる() throws IOException {
      // Arrange
      LocalDateTime dateTime = LocalDateTime.of(2026, 3, 21, 9, 30);
      UUID userId1 =
          testData.createUser(
              "test1@example.com", "password1", "テスト 一郎", "test1", "GENERAL", dateTime);
      UUID userId2 =
          testData.createUser(
              "test2@example.com", "password2", "テスト 二郎", "test2", "ADMIN", dateTime);

      String csvContent =
          "\uFEFF"
              + "権限,氏名,メールアドレス,ユーザー名\n"
              + "一般,更新 一郎,test1@example.com,updated1\n"
              + "管理者,更新 二郎,updated2@example.com,updated2\n";

      MultipartFile file =
          new MockMultipartFile(
              "file",
              "users.csv",
              "text/csv",
              new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

      // Act
      UserImportCommand command = UserImportCommand.from(file, userId1);
      UserImportResponseDto response = userApplicationService.importUsersCsv(command);

      // Assert
      assertEquals(2, response.importedCount());

      List<User> users =
          userRepository.findAllByOrderByCreatedAtAscIdAsc().stream()
              .sorted(Comparator.comparing(user -> user.emailAddress().value()))
              .toList();
      assertEquals(2, users.size());
      assertEquals(userId1, users.get(0).id());
      assertTrue(userRepository.findUserById(userId1).isPresent());
      assertEquals("test1@example.com", users.get(0).emailAddress().value());
      assertEquals("更新 一郎", users.get(0).realName().value());
      assertEquals("updated1", users.get(0).userName().value());
      assertEquals("updated2@example.com", users.get(1).emailAddress().value());
      assertEquals("更新 二郎", users.get(1).realName().value());
      assertEquals("updated2", users.get(1).userName().value());

      // パスワードがランダム生成され、既存パスワードが引き継がれていないことを確認
      PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      assertNotNull(users.get(0).password().value());
      assertNotNull(users.get(1).password().value());
      assertNotEquals(users.get(0).password().value(), users.get(1).password().value());
      assertTrue(!passwordEncoder.matches("password1", users.get(0).password().value()));
      assertTrue(!passwordEncoder.matches("password2", users.get(1).password().value()));
    }

    @Test
    void ファイル未指定の場合ResponseStatusExceptionを投げる() {
      // Arrange
      UUID currentUserId = UUID.randomUUID();

      // Act & Assert
      ResponseStatusException ex =
          assertThrows(
              ResponseStatusException.class, () -> UserImportCommand.from(null, currentUserId));
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
      assertEquals("CSVファイルを指定してください", ex.getReason());
    }

    @Test
    void 現在ログイン中ユーザーがCSVに含まれていない場合ResponseStatusExceptionを投げる() throws IOException {
      // Arrange
      LocalDateTime dateTime = LocalDateTime.of(2026, 3, 21, 9, 30);
      UUID userId1 =
          testData.createUser(
              "test1@example.com", "password1", "テスト 一郎", "test1", "GENERAL", dateTime);

      String csvContent =
          "\uFEFF" + "権限,氏名,メールアドレス,ユーザー名\n" + "管理者,新規 ユーザー,new@example.com,newuser\n";

      MultipartFile file =
          new MockMultipartFile(
              "file",
              "users.csv",
              "text/csv",
              new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

      // Act & Assert
      ResponseStatusException ex =
          assertThrows(
              ResponseStatusException.class,
              () -> userApplicationService.importUsersCsv(UserImportCommand.from(file, userId1)));
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
      assertEquals("現在ログイン中のユーザーがCSVに含まれていません", ex.getReason());
    }

    @Test
    void ADMINユーザーがCSVに含まれていない場合ResponseStatusExceptionを投げる() throws IOException {
      // Arrange
      LocalDateTime dateTime = LocalDateTime.of(2026, 3, 21, 9, 30);
      UUID userId =
          testData.createUser(
              "test@example.com", "password", "テスト 一郎", "test1", "GENERAL", dateTime);

      String csvContent = "\uFEFF" + "権限,氏名,メールアドレス,ユーザー名\n" + "一般,テスト 一郎,test@example.com,test1\n";

      MultipartFile file =
          new MockMultipartFile(
              "file",
              "users.csv",
              "text/csv",
              new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

      // Act & Assert
      ResponseStatusException ex =
          assertThrows(
              ResponseStatusException.class,
              () -> userApplicationService.importUsersCsv(UserImportCommand.from(file, userId)));
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
      assertEquals("管理者ユーザーを1人以上含めてください", ex.getReason());
    }

    @Test
    void CSVヘッダが不正な場合ResponseStatusExceptionを投げる() throws IOException {
      // Arrange
      UUID currentUserId = UUID.randomUUID();

      String csvContent =
          "\uFEFF" + "不正なヘッダ,氏名,メールアドレス,ユーザー名\n" + "一般,テスト 一郎,test@example.com,test1\n";

      MultipartFile file =
          new MockMultipartFile(
              "file",
              "users.csv",
              "text/csv",
              new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

      // Act & Assert
      ResponseStatusException ex =
          assertThrows(
              ResponseStatusException.class,
              () ->
                  userApplicationService.importUsersCsv(
                      UserImportCommand.from(file, currentUserId)));
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
      assertEquals("CSVヘッダが不正です", ex.getReason());
    }

    @Test
    void CSVファイル形式が不正な場合ResponseStatusExceptionを投げる() throws IOException {
      // Arrange
      UUID currentUserId = UUID.randomUUID();

      String csvContent = "test content";
      MultipartFile file =
          new MockMultipartFile(
              "file",
              "users.txt",
              "text/plain",
              new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

      // Act & Assert
      ResponseStatusException ex =
          assertThrows(
              ResponseStatusException.class,
              () ->
                  userApplicationService.importUsersCsv(
                      UserImportCommand.from(file, currentUserId)));
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
      assertEquals("CSVファイル形式が不正です", ex.getReason());
    }
  }

  @Nested
  class ユーザー登録 {

    @Test
    void ユーザーを新規登録できる() {
      // Act
      UserCreateRequest createRequest =
          new UserCreateRequest(
              "山田 太郎", "yamada", "yamada@example.com", "password", "password", null, "GENERAL");
      userApplicationService.createUser(createRequest.toCommand());

      // Assert
      Integer cnt =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM users WHERE email_address = ?",
              Integer.class,
              "yamada@example.com");
      assertEquals(1, cnt);
    }

    @Test
    void メールアドレスが既存ユーザーと重複するとBadRequestExceptionを投げる() {
      // Arrange
      testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");

      // Act & Assert
      UserCreateRequest createRequest =
          new UserCreateRequest(
              "鈴木 花子", "suzuki", "yamada@example.com", "password", "password", null, "GENERAL");
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> userApplicationService.createUser(createRequest.toCommand()));
      assertEquals("このメールアドレスはすでに使用されています", exception.getMessage());
    }

    @Test
    void ユーザー名が既存ユーザーと重複するとBadRequestExceptionを投げる() {
      // Arrange
      testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");

      // Act & Assert
      UserCreateRequest createRequest =
          new UserCreateRequest(
              "鈴木 花子", "yamada", "suzuki@example.com", "password", "password", null, "GENERAL");
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> userApplicationService.createUser(createRequest.toCommand()));
      assertEquals("このユーザー名はすでに使用されています", exception.getMessage());
    }
  }

  @Nested
  class ユーザー更新 {

    @Test
    void メールアドレスが他のユーザーと重複するとBadRequestExceptionを投げる() {
      // Arrange
      testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      UUID targetUserId =
          testData.createUser("suzuki@example.com", "password", "鈴木 花子", "suzuki", "GENERAL");

      // Act & Assert
      UserUpdateRequest updateRequest =
          new UserUpdateRequest(null, "鈴木 花子", "suzuki", "yamada@example.com", null);
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> userApplicationService.updateUser(updateRequest.toCommand(targetUserId)));
      assertEquals("このメールアドレスはすでに使用されています", exception.getMessage());
    }

    @Test
    void ユーザー名が他のユーザーと重複するとBadRequestExceptionを投げる() {
      // Arrange
      testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      UUID targetUserId =
          testData.createUser("suzuki@example.com", "password", "鈴木 花子", "suzuki", "GENERAL");

      // Act & Assert
      UserUpdateRequest updateRequest =
          new UserUpdateRequest(null, "鈴木 花子", "yamada", "suzuki@example.com", null);
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> userApplicationService.updateUser(updateRequest.toCommand(targetUserId)));
      assertEquals("このユーザー名はすでに使用されています", exception.getMessage());
    }

    @Test
    void 自分自身のメールアドレスとユーザー名を変更せずに更新できる() {
      // Arrange
      UUID targetUserId =
          testData.createUser("suzuki@example.com", "password", "鈴木 花子", "suzuki", "GENERAL");

      // Act
      UserUpdateRequest updateRequest =
          new UserUpdateRequest(null, "鈴木 太郎", "suzuki", "suzuki@example.com", null);
      userApplicationService.updateUser(updateRequest.toCommand(targetUserId));

      // Assert
      User updatedUser = userRepository.findUserById(targetUserId).orElseThrow();
      assertEquals("鈴木 太郎", updatedUser.realName().value());
      assertEquals("suzuki", updatedUser.userName().value());
      assertEquals("suzuki@example.com", updatedUser.emailAddress().value());
    }
  }

  @Nested
  class ログイン履歴更新 {

    @Test
    void ログイン履歴が作成されその後は更新される() {
      // Arrange
      UUID userId =
          testData.createUser("yamada@example.com", "password", "山田 太郎", "yamada", "GENERAL");
      LoginHistoryCreateCommand command = new LoginHistoryCreateCommand("yamada@example.com");

      // Act - 初回
      userApplicationService.updateUserLoginHistory(command);

      // Assert - 履歴が1件作成される
      Integer cnt =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM user_login_histories WHERE user_id = ?", Integer.class, userId);
      assertEquals(1, cnt);

      // Act - 2回目
      userApplicationService.updateUserLoginHistory(command);

      // Assert - 件数は増えず更新される
      Integer cntAfter =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM user_login_histories WHERE user_id = ?", Integer.class, userId);
      assertEquals(1, cntAfter);
    }

    /** 認証成功後に呼ばれるため通常は発生しないが、401に化けず404として扱われることを担保する。 */
    @Test
    void 対象ユーザーが存在しない場合はResourceNotFoundExceptionを投げる() {
      // Arrange
      LoginHistoryCreateCommand command = new LoginHistoryCreateCommand("notfound@example.com");

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> userApplicationService.updateUserLoginHistory(command));
    }
  }

  @Nested
  class 進捗率取得 {

    /** 進捗率はコース配下のレッスン数に依存するため、既存のコースを消してから検証する。 */
    @BeforeEach
    void deleteCourses() {
      jdbcTemplate.execute("DELETE FROM courses");
    }

    @Test
    void 進捗率を取得できる() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 3つのレッスンを作成
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

      // Userを作成
      UUID userId =
          testData.createUser("test@example.com", "password", "テスト 太郎", "testuser", "GENERAL");

      // 2つのUserLessonを作成
      testData.createUserLesson(userId, lesson1Id);
      testData.createUserLesson(userId, lesson2Id);

      int completedLessonCnt = 2;
      int allLessonsCnt = 3;
      BigDecimal progressRate =
          BigDecimal.valueOf(66.6); // completedLessonCnt / allLessonsCnt * 100

      UserSearchCommand userSearchCommand =
          new UserSearchCommand(1, 10, null, null, null, null, null, null, null);

      // Act
      UserPageDto userPageDto = userApplicationService.findUsers(userSearchCommand);

      // Assert
      assertEquals(progressRate, userPageDto.userDtos().getFirst().progressRate());
    }

    @Test
    void 総レッスン数が0のとき進捗率が0になる() {
      // Arrange
      testData.createUser("test@example.com", "password", "テスト 太郎", "testuser", "GENERAL");

      UserSearchCommand userSearchCommand =
          new UserSearchCommand(1, 10, null, null, null, null, null, null, null);

      // Act
      UserPageDto userPageDto = userApplicationService.findUsers(userSearchCommand);

      // Assert
      assertEquals(new BigDecimal("0.0"), userPageDto.userDtos().getFirst().progressRate());
    }
  }
}
