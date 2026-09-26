package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import com.everrefine.elms.domain.model.lesson.LessonSearchCriteria;
import com.everrefine.elms.domain.model.lesson.LessonWithCourseAndLessonGroup;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.infrastructure.dao.LessonDao;
import com.everrefine.elms.infrastructure.dao.LessonGroupDao;
import com.everrefine.elms.infrastructure.entity.lesson.LessonEntity;
import com.everrefine.elms.infrastructure.row.LessonGroupWithLessonRow;
import com.everrefine.elms.infrastructure.row.LessonWithCourseAndLessonGroupRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

/** {@link LessonRepository} の実装。 */
@Repository
@AllArgsConstructor
public class LessonRepositoryImpl implements LessonRepository {

  private final LessonDao lessonDao;
  private final LessonGroupDao lessonGroupDao;
  private final JdbcAggregateTemplate jdbcAggregateTemplate;

  @Override
  public Optional<Lesson> findById(UUID lessonId) {
    return lessonDao.findById(lessonId).map(LessonEntity::toDomain);
  }

  @Override
  public List<Lesson> findByIdIn(List<UUID> lessonIds) {
    if (lessonIds == null || lessonIds.isEmpty()) {
      return List.of();
    }
    return lessonDao.findByIdIn(lessonIds).stream().map(LessonEntity::toDomain).toList();
  }

  @Override
  public List<Lesson> findLessons(LessonSearchCriteria criteria) {
    UUID courseId = criteria.courseId() != null ? UUID.fromString(criteria.courseId()) : null;
    UUID lessonGroupId =
        criteria.lessonGroupId() != null ? UUID.fromString(criteria.lessonGroupId()) : null;
    LocalDate createdDateFrom = criteria.createdDateFrom();
    LocalDate createdDateTo = criteria.createdDateTo();

    return lessonDao
        .findLessons(
            courseId,
            lessonGroupId,
            criteria.title(),
            createdDateFrom,
            createdDateTo,
            criteria.getPageSize(),
            criteria.getOffset())
        .stream()
        .map(LessonEntity::toDomain)
        .toList();
  }

  @Override
  public int countLessons(LessonSearchCriteria criteria) {
    UUID courseId = criteria.courseId() != null ? UUID.fromString(criteria.courseId()) : null;
    UUID lessonGroupId =
        criteria.lessonGroupId() != null ? UUID.fromString(criteria.lessonGroupId()) : null;
    LocalDate createdDateFrom = criteria.createdDateFrom();
    LocalDate createdDateTo = criteria.createdDateTo();

    return lessonDao.countLessons(
        courseId, lessonGroupId, criteria.title(), createdDateFrom, createdDateTo);
  }

  @Override
  public List<Lesson> findLessonsByLessonGroupId(UUID lessonGroupId) {
    return lessonDao.findLessonsByLessonGroupId(lessonGroupId).stream()
        .map(LessonEntity::toDomain)
        .toList();
  }

  @Override
  public List<LessonGroupWithLessons> findLessonsGroupedByLessonGroup(UUID courseId) {
    return LessonGroupWithLessonRow.toDomainList(
        lessonGroupDao.findLessonGroupsByCourseId(courseId));
  }

  @Override
  public Lesson createLesson(Lesson lesson) {
    return lessonDao.save(LessonEntity.from(lesson)).toDomain();
  }

  /**
   * 複数のレッスンを一括登録する。
   *
   * <p>IDは呼び出し側で採番済みであること。IDが確定していると、Spring Data JDBCが採番結果の問い合わせを行わないため、 JDBCドライバの {@code
   * reWriteBatchedInserts} が複数レコードを1つのINSERT文にまとめられる。
   *
   * @param lessons 登録するレッスンリスト（IDは採番済み）
   */
  @Override
  public void createLessons(List<Lesson> lessons) {
    if (lessons.isEmpty()) {
      return;
    }

    jdbcAggregateTemplate.insertAll(lessons.stream().map(LessonEntity::from).toList());
  }

  @Override
  public Lesson updateLesson(Lesson lesson) {
    return lessonDao.save(LessonEntity.from(lesson)).toDomain();
  }

  @Override
  public Optional<BigDecimal> findMaxLessonOrderByLessonGroupId(UUID lessonGroupId) {
    return lessonDao.findMaxLessonOrderByLessonGroupId(lessonGroupId);
  }

  @Override
  public void deleteLessonById(UUID lessonId) {
    lessonDao.deleteById(lessonId);
  }

  @Override
  public void deleteLessonsByCourseId(UUID courseId) {
    lessonDao.deleteByCourseId(courseId);
  }

  @Override
  public int countAllLessons() {
    return lessonDao.countAllLessons();
  }

  @Override
  public List<String> findByVideoUrlStartingWith(String prefix) {
    return lessonDao.findByVideoUrlStartingWith(prefix);
  }

  @Override
  public List<LessonWithCourseAndLessonGroup> findAllLessons() {
    return lessonDao.findByAllLessons().stream()
        .map(LessonWithCourseAndLessonGroupRow::toDomain)
        .toList();
  }
}
