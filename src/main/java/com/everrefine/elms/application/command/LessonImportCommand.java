package com.everrefine.elms.application.command;

import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.util.CsvImportUtils;
import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.lesson.VideoUrl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** CSV取込用レッスンのコマンド。CSV全体のレッスン情報を保持する。 */
public record LessonImportCommand(UUID courseId, List<LessonImportRowCommand> rows) {

  private static final String[] EXPECTED_HEADER = {
    "レッスングループタイトル", "レッスンタイトル", "レッスン説明", "レッスンの動画URL"
  };
  private static final int LESSON_GROUP_TITLE_COLUMN_INDEX = 0;
  private static final int LESSON_TITLE_COLUMN_INDEX = 1;
  private static final int CONTENT_COLUMN_INDEX = 2;
  private static final int VIDEO_URL_COLUMN_INDEX = 3;
  private static final int LESSON_GROUP_TITLE_MAX_LENGTH = 100;
  private static final int LESSON_TITLE_MAX_LENGTH = 255;
  private static final int CONTENT_MAX_LENGTH = 1_000_000;

  /**
   * 取込用Commandを生成する。行リストは不変化される。
   *
   * @param courseId 取り込み対象コースID
   * @param rows 行Commandのリスト
   */
  public LessonImportCommand {
    rows = List.copyOf(rows);
  }

  /**
   * CSVファイルを読み込み、取込用Commandに変換する。
   *
   * @param courseId 取り込み対象コースID
   * @param filename アップロードファイル名
   * @param fileContent アップロードファイル内容
   * @return 取込用Command
   */
  public static LessonImportCommand from(UUID courseId, String filename, byte[] fileContent) {
    throwExceptionIfCsvFileInvalid(filename, fileContent);
    List<LessonImportRowCommand> rows = readRows(fileContent);
    if (rows.isEmpty()) {
      throw new BadRequestException("取り込み対象のレッスンがありません");
    }
    return new LessonImportCommand(courseId, rows);
  }

  /**
   * 取込対象のレッスン件数を取得する。
   *
   * @return 取込レッスン件数
   */
  public int getImportedLessonCount() {
    return rows.size();
  }

  /**
   * 取込対象のレッスングループ件数を取得する。
   *
   * @return 取込レッスングループ件数
   */
  public int getImportedLessonGroupCount() {
    return getRowsByLessonGroupTitle().size();
  }

  /**
   * レッスングループタイトル単位でCSV行を集約する。順序はCSV上の初出順を保持する。
   *
   * @return レッスングループタイトルと行CommandリストのMap
   */
  public Map<String, List<LessonImportRowCommand>> getRowsByLessonGroupTitle() {
    Map<String, List<LessonImportRowCommand>> groupedRows = new LinkedHashMap<>();
    for (LessonImportRowCommand row : rows) {
      groupedRows.computeIfAbsent(row.lessonGroupTitle(), key -> new ArrayList<>()).add(row);
    }
    return groupedRows;
  }

  /**
   * CSV上の表示順から保存用の並び順を計算する。
   *
   * @param index 0始まりの表示順
   * @return 保存用の並び順
   */
  public static BigDecimal calculateOrder(int index) {
    return Order.FIRST_ORDER.add(Order.INTERVAL_ORDER.multiply(BigDecimal.valueOf(index)));
  }

  /**
   * CSVファイルが不正な場合に例外をスローする。
   *
   * @param filename アップロードファイル名
   * @param fileContent アップロードファイル内容
   */
  private static void throwExceptionIfCsvFileInvalid(String filename, byte[] fileContent) {
    CsvImportUtils.throwExceptionIfCsvFileInvalid(
        filename, fileContent == null || fileContent.length == 0, BadRequestException::new);
  }

  /**
   * CSVファイルを読み込み、行Commandのリストに変換する。
   *
   * @param fileContent アップロードファイル内容
   * @return 行Commandのリスト
   */
  private static List<LessonImportRowCommand> readRows(byte[] fileContent) {
    return CsvImportUtils.readRows(
        fileContent,
        EXPECTED_HEADER,
        LessonImportCommand::toLessonImportRowCommand,
        BadRequestException::new,
        BadRequestException.class,
        true);
  }

  /**
   * CSVの行データを行Commandに変換する。
   *
   * @param values CSVの行データ
   * @param lineNumber CSV上の行番号
   * @return 行Command
   */
  private static LessonImportRowCommand toLessonImportRowCommand(String[] values, int lineNumber) {
    String lessonGroupTitle = values[LESSON_GROUP_TITLE_COLUMN_INDEX].trim();
    String lessonTitle = values[LESSON_TITLE_COLUMN_INDEX].trim();
    String content = normalizeOptionalValue(values[CONTENT_COLUMN_INDEX]);
    String videoUrl = normalizeOptionalValue(values[VIDEO_URL_COLUMN_INDEX]);

    if (lessonGroupTitle.isEmpty()) {
      throw new BadRequestException("行" + lineNumber + ": レッスングループタイトルは必須です");
    }
    if (lessonTitle.isEmpty()) {
      throw new BadRequestException("行" + lineNumber + ": レッスンタイトルは必須です");
    }
    if (lessonGroupTitle.length() > LESSON_GROUP_TITLE_MAX_LENGTH) {
      throw new BadRequestException("行" + lineNumber + ": レッスングループタイトルは100文字以内で入力してください");
    }
    if (lessonTitle.length() > LESSON_TITLE_MAX_LENGTH) {
      throw new BadRequestException("行" + lineNumber + ": レッスンタイトルは255文字以内で入力してください");
    }
    if (content != null && content.length() > CONTENT_MAX_LENGTH) {
      throw new BadRequestException("行" + lineNumber + ": レッスン説明は1000000文字以内で入力してください");
    }
    if (videoUrl != null && videoUrl.length() > VideoUrl.MAX_LENGTH) {
      throw new BadRequestException("行" + lineNumber + ": レッスンの動画URLは2048文字以内で入力してください");
    }

    return new LessonImportRowCommand(lessonGroupTitle, lessonTitle, content, videoUrl);
  }

  private static String normalizeOptionalValue(String value) {
    String trimmedValue = value.trim();
    return trimmedValue.isEmpty() ? null : trimmedValue;
  }
}
