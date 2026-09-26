package com.everrefine.elms.domain.model.course;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.ThumbnailUrl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.lang.Nullable;

/** コースのドメインモデル。 */
public record Course(
    UUID id,
    @Nullable ThumbnailUrl thumbnailUrl,
    CourseTitle title,
    @Nullable CourseDescription description,
    Order courseOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * 新規作成用のコースを作成する。
   *
   * @param thumbnailUrl サムネイル画像URL
   * @param title タイトル
   * @param description 説明
   * @param courseOrder コースの並び順
   * @return 新規作成用のコース
   */
  public static Course create(
      @Nullable String thumbnailUrl,
      String title,
      @Nullable String description,
      BigDecimal courseOrder) {
    return new Course(
        null,
        thumbnailUrl == null ? null : new ThumbnailUrl(thumbnailUrl),
        new CourseTitle(title),
        description == null ? null : new CourseDescription(description),
        new Order(courseOrder),
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  /**
   * 更新用のコースを作成する。
   *
   * @param thumbnailUrl サムネイル画像URL
   * @param title タイトル
   * @param description 説明
   * @param courseOrder コースの並び順
   * @return 更新用のコース
   */
  public Course update(
      @Nullable String thumbnailUrl,
      String title,
      @Nullable String description,
      BigDecimal courseOrder) {
    return new Course(
        id,
        thumbnailUrl == null ? null : new ThumbnailUrl(thumbnailUrl),
        new CourseTitle(title),
        description == null ? null : new CourseDescription(description),
        new Order(courseOrder),
        createdAt,
        LocalDateTime.now());
  }
}
