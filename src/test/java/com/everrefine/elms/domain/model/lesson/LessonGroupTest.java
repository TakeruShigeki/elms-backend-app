package com.everrefine.elms.domain.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.domain.exception.InvalidValueException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LessonGroupTest {

  @Nested
  class レッスングループ作成 {
    @Test
    void createで新しいレッスングループが作成される() {
      // Arrange & Act
      UUID courseId = UUID.randomUUID();
      LessonGroup lessonGroup = LessonGroup.create(courseId, new BigDecimal("1"), "テストグループ");

      // Assert
      assertEquals(null, lessonGroup.id());
      assertEquals(courseId, lessonGroup.courseId());
      assertEquals(new BigDecimal("1"), lessonGroup.lessonGroupOrder().value());
      assertEquals("テストグループ", lessonGroup.title().value());
      assertNotNull(lessonGroup.createdAt()); // createメソッドで設定される
      assertNotNull(lessonGroup.updatedAt()); // createメソッドで設定される
    }
  }

  @Nested
  class レッスングループ更新 {
    @Test
    void updateで新しいインスタンスが作成され不変性が保たれる() {
      // Arrange
      LocalDateTime originalCreatedAt = LocalDateTime.of(2025, 12, 10, 10, 0);
      LocalDateTime originalUpdatedAt = LocalDateTime.of(2025, 12, 10, 10, 0);

      LessonGroup original =
          new LessonGroup(
              UUID.randomUUID(),
              UUID.randomUUID(),
              new com.everrefine.elms.domain.model.Order(new BigDecimal("1")),
              new LessonTitle("元のタイトル"),
              originalCreatedAt,
              originalUpdatedAt);

      // Act
      LocalDateTime beforeUpdate = LocalDateTime.now();
      LessonGroup updated = original.update("新しいタイトル");
      LocalDateTime afterUpdate = LocalDateTime.now();

      // Assert - 新しいインスタンスが作成されていること
      assertNotEquals(original, updated);

      // Assert - 不変なフィールドが保持されていること
      assertEquals(original.id(), updated.id());
      assertEquals(original.courseId(), updated.courseId());
      assertEquals(original.lessonGroupOrder(), updated.lessonGroupOrder());
      assertEquals(original.createdAt(), updated.createdAt());

      // Assert - タイトルが更新されていること
      assertEquals("新しいタイトル", updated.title().value());
      assertEquals("元のタイトル", original.title().value());

      // Assert - updatedAtが更新されていること
      assertNotEquals(original.updatedAt(), updated.updatedAt());
      assertTrue(
          updated.updatedAt().isAfter(beforeUpdate) || updated.updatedAt().isEqual(beforeUpdate));
      assertTrue(
          updated.updatedAt().isBefore(afterUpdate) || updated.updatedAt().isEqual(afterUpdate));

      // Assert - 元のインスタンスは変更されていないこと
      assertEquals("元のタイトル", original.title().value());
      assertEquals(originalUpdatedAt, original.updatedAt());
    }

    @Test
    void updateで同じタイトルを設定しても新しいインスタンスが作成される() {
      // Arrange
      LocalDateTime originalCreatedAt = LocalDateTime.of(2025, 12, 10, 10, 0);
      LocalDateTime originalUpdatedAt = LocalDateTime.of(2025, 12, 10, 10, 0);

      LessonGroup original =
          new LessonGroup(
              UUID.randomUUID(),
              UUID.randomUUID(),
              new com.everrefine.elms.domain.model.Order(new BigDecimal("1")),
              new LessonTitle("同じタイトル"),
              originalCreatedAt,
              originalUpdatedAt);

      // Act
      LessonGroup updated = original.update("同じタイトル");

      // Assert - 新しいインスタンスが作成されていること
      assertNotEquals(original, updated);
      // Assert - updatedAtは更新されること（LocalDateTime.now()が呼ばれるため）
      assertNotEquals(original.updatedAt(), updated.updatedAt());
    }

    @Test
    void updateで空文字を設定できる() {
      // Arrange
      LessonGroup original =
          new LessonGroup(
              UUID.randomUUID(),
              UUID.randomUUID(),
              new com.everrefine.elms.domain.model.Order(new BigDecimal("1")),
              new LessonTitle("元のタイトル"),
              LocalDateTime.now(),
              LocalDateTime.now());

      // Act
      LessonGroup updated = original.update("");

      // Assert
      assertEquals("", updated.title().value());
    }

    @Test
    void updateでnullを設定するとInvalidValueExceptionを投げる() {
      // Arrange
      LessonGroup original =
          new LessonGroup(
              UUID.randomUUID(),
              UUID.randomUUID(),
              new com.everrefine.elms.domain.model.Order(new BigDecimal("1")),
              new LessonTitle("元のタイトル"),
              LocalDateTime.now(),
              LocalDateTime.now());

      // Act & Assert
      assertThrows(InvalidValueException.class, () -> original.update(null));
    }
  }
}
