package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.lesson.LessonGroup;
import com.everrefine.elms.domain.repository.LessonGroupRepository;
import com.everrefine.elms.infrastructure.dao.LessonGroupDao;
import com.everrefine.elms.infrastructure.entity.lesson.LessonGroupEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

/** {@link LessonGroupRepository} の実装。 */
@Repository
@AllArgsConstructor
public class LessonGroupRepositoryImpl implements LessonGroupRepository {

  private final LessonGroupDao lessonGroupDao;
  private final JdbcAggregateTemplate jdbcAggregateTemplate;

  @Override
  public LessonGroup createLessonGroup(LessonGroup lessonGroup) {
    return lessonGroupDao.save(LessonGroupEntity.from(lessonGroup)).toDomain();
  }

  /**
   * 複数のレッスングループを一括登録する。
   *
   * <p>IDは呼び出し側で採番済みであること。IDが確定していると、Spring Data JDBCが採番結果の問い合わせを行わないため、 JDBCドライバの {@code
   * reWriteBatchedInserts} が複数レコードを1つのINSERT文にまとめられる。
   *
   * @param lessonGroups 登録するレッスングループリスト（IDは採番済み）
   */
  @Override
  public void createLessonGroups(List<LessonGroup> lessonGroups) {
    if (lessonGroups.isEmpty()) {
      return;
    }

    jdbcAggregateTemplate.insertAll(lessonGroups.stream().map(LessonGroupEntity::from).toList());
  }

  @Override
  public LessonGroup updateLessonGroup(LessonGroup lessonGroup) {
    return lessonGroupDao.save(LessonGroupEntity.from(lessonGroup)).toDomain();
  }

  @Override
  public Optional<BigDecimal> findMaxLessonGroupOrderByCourseId(UUID courseId) {
    return lessonGroupDao.findMaxLessonGroupOrderByCourseId(courseId);
  }

  @Override
  public Optional<LessonGroup> findLessonGroupById(UUID id) {
    return lessonGroupDao.findById(id).map(LessonGroupEntity::toDomain);
  }

  @Override
  public void deleteLessonGroupById(UUID id) {
    lessonGroupDao.deleteById(id);
  }

  @Override
  public void deleteLessonGroupsByCourseId(UUID courseId) {
    lessonGroupDao.deleteByCourseId(courseId);
  }
}
