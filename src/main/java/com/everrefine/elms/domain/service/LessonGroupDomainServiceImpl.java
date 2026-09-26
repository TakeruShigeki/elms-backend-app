package com.everrefine.elms.domain.service;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.repository.LessonGroupRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/** {@link LessonGroupDomainService} の実装。 */
@Component
@AllArgsConstructor
public class LessonGroupDomainServiceImpl implements LessonGroupDomainService {

  private final LessonGroupRepository lessonGroupRepository;

  @Override
  public BigDecimal issueLessonGroupOrder(UUID courseId) {
    return lessonGroupRepository
        .findMaxLessonGroupOrderByCourseId(courseId)
        .map(maxOrder -> maxOrder.add(Order.INTERVAL_ORDER))
        .orElse(Order.FIRST_ORDER);
  }
}
