package com.everrefine.elms.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

/** レッスングループに関するドメインサービスインターフェース。 */
public interface LessonGroupDomainService {

  /**
   * レッスングループの並び順を発番する。 指定されたコース内で最大のlesson_group_order + 1024を返す。 レッスングループが存在しない場合は1024を返す。
   *
   * @param courseId コースID
   * @return 発番されたレッスングループの並び順
   */
  BigDecimal issueLessonGroupOrder(UUID courseId);
}
