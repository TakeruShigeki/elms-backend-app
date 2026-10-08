package com.everrefine.elms.domain.model.lesson;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.tag.Tag;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    List<Tag> tags,
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
        List.of(),
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
        id,
        lessonGroupId,
        courseId,
        lessonOrder,
        title,
        content,
        videoUrl,
        tags,
        createdAt,
        updatedAt);
  }

  /**
   * 更新用のレッスンを作成する。
   *
   * @param title レッスンタイトル
   * @param content レッスンの本文
   * @param videoUrl レッスンの動画URL
   * @param tags タグ名のリスト（{@code null} の場合はタグなし）
   * @return 更新用のレッスン
   */
  public Lesson update(String title, String content, String videoUrl, List<String> tags) {
    return new Lesson(
        this.id,
        this.lessonGroupId,
        this.courseId,
        this.lessonOrder,
        title == null ? this.title : new LessonTitle(title),
        content == null ? this.content : new LessonContent(content),
        videoUrl == null ? this.videoUrl : new VideoUrl(videoUrl),
        tags == null
            ? List.of()
            : tags.stream().map(name -> new Tag(null, name)).distinct().toList(),
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
        this.tags,
        this.createdAt,
        LocalDateTime.now());
  }
}
