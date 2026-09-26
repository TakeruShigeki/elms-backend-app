package com.everrefine.elms.domain.model.lesson;

import com.everrefine.elms.domain.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.lang.Nullable;

/** レッスンのドメインモデル。 */
public record Lesson(
    UUID id,
    UUID lessonGroupId,
    UUID courseId,
    Order lessonOrder,
    LessonTitle title,
    @Nullable LessonContent content,
    @Nullable VideoUrl videoUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * 新規作成用のレッスンを作成する。
   *
   * @param lessonGroupId レッスングループID
   * @param courseId コースID
   * @param lessonOrder レッスンの並び順
   * @param title レッスンタイトル
   * @param content レッスンの本文
   * @param videoUrl レッスンの動画URL
   * @return 新規作成用のレッスン
   */
  public static Lesson create(
      UUID lessonGroupId,
      UUID courseId,
      BigDecimal lessonOrder,
      String title,
      String content,
      String videoUrl) {
    return new Lesson(
        null,
        lessonGroupId,
        courseId,
        new Order(lessonOrder),
        new LessonTitle(title),
        content == null ? null : new LessonContent(content),
        videoUrl == null ? null : new VideoUrl(videoUrl),
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  /**
   * IDを設定したレッスンを返す。
   *
   * <p>一括登録では {@code save()} を経由せず {@code insertAll()} で直接INSERTするため、DB採番に頼らずアプリケーション側でIDを確定できる。
   * IDが確定していると、JDBCドライバが複数レコードを1つのINSERT文にまとめられる。
   *
   * @param id レッスンID
   * @return IDを設定したレッスン
   */
  public Lesson withId(UUID id) {
    return new Lesson(
        id, lessonGroupId, courseId, lessonOrder, title, content, videoUrl, createdAt, updatedAt);
  }

  /**
   * 更新用のレッスンを作成する。
   *
   * @param title レッスンタイトル
   * @param content レッスンの本文
   * @param videoUrl レッスンの動画URL
   * @return 更新用のレッスン
   */
  public Lesson update(String title, String content, String videoUrl) {
    return new Lesson(
        this.id,
        this.lessonGroupId,
        this.courseId,
        this.lessonOrder,
        title == null ? this.title : new LessonTitle(title),
        content == null ? this.content : new LessonContent(content),
        videoUrl == null ? this.videoUrl : new VideoUrl(videoUrl),
        this.createdAt,
        LocalDateTime.now());
  }

  /**
   * レッスンの並び順を変更する。
   *
   * @param newOrder 新しい並び順
   * @return 並び順が変更されたレッスン
   */
  public Lesson updateOrder(BigDecimal newOrder) {
    return new Lesson(
        this.id,
        this.lessonGroupId,
        this.courseId,
        new Order(newOrder),
        this.title,
        this.content,
        this.videoUrl,
        this.createdAt,
        LocalDateTime.now());
  }
}
