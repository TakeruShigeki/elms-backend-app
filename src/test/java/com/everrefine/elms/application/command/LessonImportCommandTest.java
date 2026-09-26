package com.everrefine.elms.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.everrefine.elms.application.exception.BadRequestException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** {@link LessonImportCommand} の単体テスト。 */
class LessonImportCommandTest {

  private static final String HEADER = "レッスングループタイトル,レッスンタイトル,レッスン説明,レッスンの動画URL";

  @Nested
  class CSV読み込み {
    @Test
    void BOM付きCSVとクォート付きカンマを読み込める() {
      String csv =
          "\uFEFF"
              + String.join(
                  "\n",
                  HEADER,
                  "Basic,\"Lesson, 1\",\"説明, 1\",https://example.com/1.mp4",
                  "Basic,Lesson 2,,");

      LessonImportCommand command =
          LessonImportCommand.from(
              UUID.randomUUID(), "lessons.csv", csv.getBytes(StandardCharsets.UTF_8));

      assertEquals(2, command.getImportedLessonCount());
      assertEquals(1, command.getImportedLessonGroupCount());
      assertEquals(new BigDecimal("1024"), LessonImportCommand.calculateOrder(0));
      assertEquals(new BigDecimal("2048"), LessonImportCommand.calculateOrder(1));

      Map<String, List<LessonImportRowCommand>> rowsByGroup = command.getRowsByLessonGroupTitle();
      List<LessonImportRowCommand> rows = rowsByGroup.get("Basic");
      assertEquals("Lesson, 1", rows.get(0).lessonTitle());
      assertEquals("説明, 1", rows.get(0).content());
      assertEquals("https://example.com/1.mp4", rows.get(0).videoUrl());
      assertEquals("Lesson 2", rows.get(1).lessonTitle());
      assertNull(rows.get(1).content());
      assertNull(rows.get(1).videoUrl());
    }
  }

  @Nested
  class CSVバリデーション {
    @Test
    void CSVファイル形式が不正な場合BadRequestExceptionを投げる() {
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  LessonImportCommand.from(
                      UUID.randomUUID(), "lessons.txt", HEADER.getBytes(StandardCharsets.UTF_8)));

      assertEquals("CSVファイル形式が不正です", exception.getMessage());
    }

    @Test
    void ヘッダのみの場合BadRequestExceptionを投げる() {
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  LessonImportCommand.from(
                      UUID.randomUUID(), "lessons.csv", HEADER.getBytes(StandardCharsets.UTF_8)));

      assertEquals("取り込み対象のレッスンがありません", exception.getMessage());
    }

    @Test
    void ヘッダが不正な場合BadRequestExceptionを投げる() {
      String csv = String.join("\n", "不正ヘッダ,レッスンタイトル,レッスン説明,レッスンの動画URL", "Basic,L1,,");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  LessonImportCommand.from(
                      UUID.randomUUID(), "lessons.csv", csv.getBytes(StandardCharsets.UTF_8)));

      assertEquals("CSVヘッダが不正です", exception.getMessage());
    }

    @Test
    void クォートが閉じていない場合BadRequestExceptionを投げる() {
      String csv = String.join("\n", HEADER, "Basic,\"Lesson 1,,");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  LessonImportCommand.from(
                      UUID.randomUUID(), "lessons.csv", csv.getBytes(StandardCharsets.UTF_8)));

      assertEquals("CSVファイルの形式が不正です", exception.getMessage());
    }

    @Test
    void 列数が不正な場合BadRequestExceptionを投げる() {
      String csv = String.join("\n", HEADER, "Basic,L1");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  LessonImportCommand.from(
                      UUID.randomUUID(), "lessons.csv", csv.getBytes(StandardCharsets.UTF_8)));

      assertEquals("行2: 列数が不正です", exception.getMessage());
    }

    @Test
    void 必須項目が未入力の場合BadRequestExceptionを投げる() {
      String csv = String.join("\n", HEADER, ",L1,,");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  LessonImportCommand.from(
                      UUID.randomUUID(), "lessons.csv", csv.getBytes(StandardCharsets.UTF_8)));

      assertEquals("行2: レッスングループタイトルは必須です", exception.getMessage());
    }
  }
}
