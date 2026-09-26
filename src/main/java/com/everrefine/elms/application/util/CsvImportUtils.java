package com.everrefine.elms.application.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * CSV取込処理で共通利用するユーティリティ。 CSVファイルの拡張子検証、BOM除去、ヘッダ検証、行パースなど、取込対象に依存しない処理を提供する。
 * 取込対象ごとの行変換処理や例外型は呼び出し元から渡すことで、各Commandの既存挙動を維持する。
 */
public final class CsvImportUtils {

  private CsvImportUtils() {}

  /**
   * CSVファイルが未指定、または拡張子がCSVでない場合に例外をスローする。
   *
   * @param filename アップロードファイル名
   * @param contentMissing ファイル内容が未指定の場合はtrue
   * @param exceptionFactory エラーメッセージから例外を生成する関数
   * @throws RuntimeException ファイル未指定、または拡張子がCSVでない場合
   */
  public static void throwExceptionIfCsvFileInvalid(
      String filename,
      boolean contentMissing,
      Function<String, ? extends RuntimeException> exceptionFactory) {
    if (contentMissing) {
      throw exceptionFactory.apply("CSVファイルを指定してください");
    }

    if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
      throw exceptionFactory.apply("CSVファイル形式が不正です");
    }
  }

  /**
   * CSVファイルを読み込み、行Commandのリストに変換する。
   *
   * @param <T> CSV行から変換する戻り値の型
   * @param fileContent アップロードファイル内容
   * @param expectedHeader 期待するCSVヘッダ
   * @param rowMapper CSV行からCommandへ変換する関数
   * @param exceptionFactory エラーメッセージから例外を生成する関数
   * @param passthroughExceptionType そのまま再throwする例外型
   * @param rejectUnclosedQuotes クォート未クローズをCSV形式不正として扱う場合はtrue
   * @return 行Commandのリスト
   * @throws RuntimeException CSVが空、ヘッダ不正、列数不正、行変換失敗、またはCSV解析失敗の場合
   */
  public static <T> List<T> readRows(
      byte[] fileContent,
      String[] expectedHeader,
      CsvRowMapper<T> rowMapper,
      Function<String, ? extends RuntimeException> exceptionFactory,
      Class<? extends RuntimeException> passthroughExceptionType,
      boolean rejectUnclosedQuotes) {
    List<T> rows = new ArrayList<>();

    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(fileContent), StandardCharsets.UTF_8))) {
      skipBom(reader);

      String headerLine = reader.readLine();
      if (headerLine == null) {
        throw exceptionFactory.apply("CSVファイルが空です");
      }

      throwExceptionIfCsvHeaderInvalid(
          parseCsvLine(headerLine, exceptionFactory, rejectUnclosedQuotes),
          expectedHeader,
          exceptionFactory);

      String line;
      int lineNumber = 1;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.trim().isEmpty()) {
          continue;
        }

        String[] values = parseCsvLine(line, exceptionFactory, rejectUnclosedQuotes);
        if (values.length != expectedHeader.length) {
          throw exceptionFactory.apply("行" + lineNumber + ": 列数が不正です");
        }

        rows.add(rowMapper.map(values, lineNumber));
      }
    } catch (RuntimeException e) {
      if (passthroughExceptionType.isInstance(e)) {
        throw e;
      }
      throw exceptionFactory.apply("CSVファイルの解析に失敗しました: " + e.getMessage());
    } catch (Exception e) {
      throw exceptionFactory.apply("CSVファイルの解析に失敗しました: " + e.getMessage());
    }

    return rows;
  }

  /**
   * UTF-8 BOMがある場合は読み飛ばす。
   *
   * @param reader CSV読み込み用Reader
   * @throws java.io.IOException Readerの読み込みまたはリセットに失敗した場合
   */
  private static void skipBom(BufferedReader reader) throws java.io.IOException {
    reader.mark(1);
    int bom = reader.read();
    if (bom != 0xFEFF) {
      reader.reset();
    }
  }

  /**
   * CSVヘッダが期待するヘッダと一致しない場合に例外をスローする。
   *
   * @param actualHeader アップロードCSVのヘッダ
   * @param expectedHeader 期待するCSVヘッダ
   * @param exceptionFactory エラーメッセージから例外を生成する関数
   * @throws RuntimeException ヘッダの列数、または列名が一致しない場合
   */
  private static void throwExceptionIfCsvHeaderInvalid(
      String[] actualHeader,
      String[] expectedHeader,
      Function<String, ? extends RuntimeException> exceptionFactory) {
    if (actualHeader.length != expectedHeader.length) {
      throw exceptionFactory.apply("CSVヘッダが不正です");
    }

    for (int i = 0; i < expectedHeader.length; i++) {
      if (!expectedHeader[i].equals(actualHeader[i])) {
        throw exceptionFactory.apply("CSVヘッダが不正です");
      }
    }
  }

  /**
   * CSVの1行をパースして値配列に変換する。 ダブルクォートで囲まれたカンマと、エスケープされたダブルクォートを考慮して分割する。
   *
   * @param line CSVの1行
   * @param exceptionFactory エラーメッセージから例外を生成する関数
   * @param rejectUnclosedQuotes クォート未クローズをCSV形式不正として扱う場合はtrue
   * @return パースされた値の配列
   * @throws RuntimeException クォート未クローズをCSV形式不正として扱う設定で、クォートが閉じられていない場合
   */
  private static String[] parseCsvLine(
      String line,
      Function<String, ? extends RuntimeException> exceptionFactory,
      boolean rejectUnclosedQuotes) {
    List<String> values = new ArrayList<>();
    StringBuilder currentValue = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          currentValue.append('"');
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (c == ',' && !inQuotes) {
        values.add(currentValue.toString());
        currentValue = new StringBuilder();
      } else {
        currentValue.append(c);
      }
    }

    if (inQuotes && rejectUnclosedQuotes) {
      throw exceptionFactory.apply("CSVファイルの形式が不正です");
    }

    values.add(currentValue.toString());
    return values.toArray(new String[0]);
  }

  /**
   * CSV行を任意の型へ変換する関数。
   *
   * @param <T> CSV行から変換する戻り値の型
   */
  @FunctionalInterface
  public interface CsvRowMapper<T> {

    /**
     * CSVの値配列と物理行番号を受け取り、取込対象ごとの行オブジェクトに変換する。
     *
     * @param values CSVの値配列
     * @param lineNumber CSV上の物理行番号
     * @return 変換後の行オブジェクト
     */
    T map(String[] values, int lineNumber);
  }
}
