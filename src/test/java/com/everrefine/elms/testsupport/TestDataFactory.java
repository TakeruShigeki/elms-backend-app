package com.everrefine.elms.testsupport;

import static com.everrefine.elms.domain.model.user.Password.encryptAndCreate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 統合テスト共通のテストデータ作成ヘルパー。
 *
 * <p>テストの「前提データ」を生SQLで投入する。テスト対象のリポジトリやDAOを経由しないことで、
 * テスト対象のコードが壊れている場合に「準備中のエラー」ではなく「検証の失敗」として現れるようにする。
 *
 * <p>各テストクラスからは {@code @Autowired} で受け取る。
 *
 * <p>検証（assert）側の生SQLは、実装から独立した目でDBの状態を確認するという役割があるため、 このクラスには集約せず各テストメソッドに残すこと。
 */
@Component
public class TestDataFactory {

  private final JdbcTemplate jdbcTemplate;

  public TestDataFactory(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * コースを作成する。サムネイルURLは未設定になる。
   *
   * @param courseOrder コースの並び順
   * @param title コースタイトル
   * @param description コースの説明
   * @return 作成されたコースID
   */
  public UUID createCourse(BigDecimal courseOrder, String title, String description) {
    return createCourse(courseOrder, title, description, null);
  }

  /**
   * サムネイルURLを指定してコースを作成する。
   *
   * @param courseOrder コースの並び順
   * @param title コースタイトル
   * @param description コースの説明
   * @param thumbnailUrl サムネイル画像のURL
   * @return 作成されたコースID
   */
  public UUID createCourse(
      BigDecimal courseOrder, String title, String description, String thumbnailUrl) {
    jdbcTemplate.update(
        """
            INSERT INTO courses (course_order, title, description, thumbnail_url, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
        courseOrder,
        title,
        description,
        thumbnailUrl,
        LocalDateTime.now(),
        LocalDateTime.now());
    return jdbcTemplate.queryForObject("SELECT id FROM courses WHERE title = ?", UUID.class, title);
  }

  /**
   * レッスングループを作成する。
   *
   * @param courseId コースID
   * @param lessonGroupOrder レッスングループの並び順
   * @param title レッスングループタイトル
   * @return 作成されたレッスングループID
   */
  public UUID createLessonGroup(UUID courseId, BigDecimal lessonGroupOrder, String title) {
    jdbcTemplate.update(
        """
            INSERT INTO lesson_groups (course_id, lesson_group_order, title, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
            """,
        courseId,
        lessonGroupOrder,
        title,
        LocalDateTime.now(),
        LocalDateTime.now());
    return jdbcTemplate.queryForObject(
        "SELECT id FROM lesson_groups WHERE title = ?", UUID.class, title);
  }

  /**
   * レッスンを作成する。
   *
   * @param lessonGroupId レッスングループID
   * @param courseId コースID
   * @param lessonOrder レッスンの並び順
   * @param title レッスンタイトル
   * @param content レッスンの本文
   * @param videoUrl レッスンの動画URL
   * @return 作成されたレッスンID
   */
  public UUID createLesson(
      UUID lessonGroupId,
      UUID courseId,
      BigDecimal lessonOrder,
      String title,
      String content,
      String videoUrl) {
    jdbcTemplate.update(
        """
            INSERT INTO lessons (
              lesson_group_id, course_id, lesson_order, title, content, video_url, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
        lessonGroupId,
        courseId,
        lessonOrder,
        title,
        content,
        videoUrl,
        LocalDateTime.now(),
        LocalDateTime.now());
    return jdbcTemplate.queryForObject("SELECT id FROM lessons WHERE title = ?", UUID.class, title);
  }

  /**
   * ユーザーを作成する。作成日時は現在日時になる。
   *
   * @param emailAddress メールアドレス
   * @param password パスワード（平文。内部でハッシュ化する）
   * @param realName 本名
   * @param userName ユーザー名
   * @param userRole 権限
   * @return 作成されたユーザーID
   */
  public UUID createUser(
      String emailAddress, String password, String realName, String userName, String userRole) {
    return createUser(emailAddress, password, realName, userName, userRole, LocalDateTime.now());
  }

  /**
   * 作成日時を指定してユーザーを作成する。
   *
   * @param emailAddress メールアドレス
   * @param password パスワード（平文。内部でハッシュ化する）
   * @param realName 本名
   * @param userName ユーザー名
   * @param userRole 権限
   * @param createdAt 作成日時
   * @return 作成されたユーザーID
   */
  public UUID createUser(
      String emailAddress,
      String password,
      String realName,
      String userName,
      String userRole,
      LocalDateTime createdAt) {
    jdbcTemplate.update(
        """
            INSERT INTO users (
              email_address, password, real_name, user_name, thumbnail_url, user_role, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
        emailAddress,
        encryptAndCreate(password).value(),
        realName,
        userName,
        null,
        userRole,
        createdAt,
        LocalDateTime.now());
    return jdbcTemplate.queryForObject(
        "SELECT id FROM users WHERE email_address = ?", UUID.class, emailAddress);
  }

  /**
   * ユーザーのレッスン受講状況を作成する。
   *
   * @param userId ユーザーID
   * @param lessonId レッスンID
   */
  public void createUserLesson(UUID userId, UUID lessonId) {
    jdbcTemplate.update(
        """
            INSERT INTO user_lessons (user_id, lesson_id, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            """,
        userId,
        lessonId,
        LocalDateTime.now(),
        LocalDateTime.now());
  }
}
