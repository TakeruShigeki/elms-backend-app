package com.everrefine.elms.domain.model.user;

import com.everrefine.elms.domain.exception.InvalidValueException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** 進捗率を表す値オブジェクト。 */
public record ProgressRate(BigDecimal value) {

  /**
   * 進捗率を作成する。値は小数点第1位で切り捨てられる。
   *
   * @param value 進捗率（0.0〜100.0）
   */
  public ProgressRate {
    if (value == null) {
      throw new InvalidValueException("進捗率の値はnullにすることはできません");
    }
    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new InvalidValueException("進捗率は0.0から100.0の間である必要があります: " + value);
    }
    value = value.setScale(1, RoundingMode.DOWN);
  }

  /**
   * 完了レッスン数と総レッスン数から進捗率を算出して生成する。
   *
   * @param completedLessonCnt 完了レッスン数
   * @param allLessonsCnt 総レッスン数
   * @return 進捗率（0.0〜100.0、小数点第1位）
   */
  public static ProgressRate of(int completedLessonCnt, int allLessonsCnt) {
    if (allLessonsCnt == 0) {
      return new ProgressRate(BigDecimal.ZERO);
    }
    BigDecimal progressRate =
        BigDecimal.valueOf(completedLessonCnt)
            .divide(BigDecimal.valueOf(allLessonsCnt), 3, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(100))
            .setScale(1, RoundingMode.DOWN);
    return new ProgressRate(progressRate);
  }

  /**
   * BigDecimal値を取得する。
   *
   * @return 進捗率のBigDecimal値
   */
  public BigDecimal toBigDecimal() {
    return value;
  }
}
