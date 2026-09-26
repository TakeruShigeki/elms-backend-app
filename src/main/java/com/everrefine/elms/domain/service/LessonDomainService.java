package com.everrefine.elms.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

/** レッスンに関するドメインサービスインターフェース。 */
public interface LessonDomainService {

  /**
   * レッスンの並び順を発番する。 指定されたレッスングループ内で最大のlesson_order + 1024を返す。 レッスンが存在しない場合は1024を返す。
   *
   * @param lessonGroupId レッスングループID
   * @return 発番されたレッスンの並び順
   */
  BigDecimal issueLessonOrder(UUID lessonGroupId);

  /**
   * 新しいレッスンの並び順を計算する。 前後のレッスンの並び順から中間値を計算する。
   *
   * @param precedingOrder 前のレッスンの並び順（先頭に移動する場合はnull）
   * @param followingOrder 後のレッスンの並び順（末尾に移動する場合はnull）
   * @return 計算された新しい並び順
   */
  BigDecimal calculateNewOrder(BigDecimal precedingOrder, BigDecimal followingOrder);

  /**
   * 計算された並び順が、前後のレッスンの間に収まっているかを判定する。
   *
   * <p>並び順は小数第4位までしか保持できないため、同じ位置への挿入を繰り返すと中間値が丸められ、 前後どちらかの並び順と同じ値になる。その状態を検知するために使用する。
   *
   * @param newOrder 計算された新しい並び順
   * @param precedingOrder 前のレッスンの並び順（先頭に移動する場合はnull）
   * @param followingOrder 後のレッスンの並び順（末尾に移動する場合はnull）
   * @return 前後のレッスンの間に収まっている場合はtrue
   */
  boolean hasRoomForNewOrder(
      BigDecimal newOrder, BigDecimal precedingOrder, BigDecimal followingOrder);
}
