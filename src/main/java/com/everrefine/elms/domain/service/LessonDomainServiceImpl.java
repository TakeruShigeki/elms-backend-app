package com.everrefine.elms.domain.service;

import com.everrefine.elms.domain.exception.InvalidValueException;
import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.repository.LessonRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** {@link LessonDomainService} の実装。 */
@Component
@RequiredArgsConstructor
public class LessonDomainServiceImpl implements LessonDomainService {

  /** 並び順の小数部の桁数。DBの桁数（numeric(10,4)）に合わせる。 */
  private static final int ORDER_SCALE = 4;

  private final LessonRepository lessonRepository;

  @Override
  public BigDecimal issueLessonOrder(UUID lessonGroupId) {
    return lessonRepository
        .findMaxLessonOrderByLessonGroupId(lessonGroupId)
        .map(maxOrder -> maxOrder.add(Order.INTERVAL_ORDER))
        .orElse(Order.FIRST_ORDER);
  }

  @Override
  public BigDecimal calculateNewOrder(BigDecimal precedingOrder, BigDecimal followingOrder) {
    if (precedingOrder == null && followingOrder == null) {
      throw new InvalidValueException("前後の並び順が両方nullにはできません");
    }

    if (precedingOrder == null) {
      return followingOrder.divide(BigDecimal.valueOf(2), ORDER_SCALE, RoundingMode.HALF_UP);
    }

    if (followingOrder == null) {
      return precedingOrder.add(Order.INTERVAL_ORDER);
    }

    return precedingOrder
        .add(followingOrder)
        .divide(BigDecimal.valueOf(2), ORDER_SCALE, RoundingMode.HALF_UP);
  }

  @Override
  public boolean hasRoomForNewOrder(
      BigDecimal newOrder, BigDecimal precedingOrder, BigDecimal followingOrder) {
    if (precedingOrder != null && newOrder.compareTo(precedingOrder) <= 0) {
      return false;
    }

    return followingOrder == null || newOrder.compareTo(followingOrder) < 0;
  }
}
