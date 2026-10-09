package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import com.everrefine.elms.domain.model.lesson.LessonSearchCriteria;
import com.everrefine.elms.domain.model.lesson.LessonWithCourseAndLessonGroup;
import com.everrefine.elms.domain.model.tag.Tag;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.infrastructure.dao.LessonDao;
import com.everrefine.elms.infrastructure.dao.LessonGroupDao;
import com.everrefine.elms.infrastructure.dao.LessonTagDao;
import com.everrefine.elms.infrastructure.dao.TagDao;
import com.everrefine.elms.infrastructure.entity.lesson.LessonEntity;
import com.everrefine.elms.infrastructure.entity.tag.LessonTagEntity;
import com.everrefine.elms.infrastructure.entity.tag.TagEntity;
import com.everrefine.elms.infrastructure.row.LessonGroupWithLessonRow;
import com.everrefine.elms.infrastructure.row.LessonWithCourseAndLessonGroupRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

/** {@link LessonRepository} の実装。 */
@Repository
@AllArgsConstructor
public class LessonRepositoryImpl implements LessonRepository {

  private final LessonDao lessonDao;
  private final LessonGroupDao lessonGroupDao;
  private final TagDao tagDao;
  private final LessonTagDao lessonTagDao;
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
    LessonEntity saved = lessonDao.save(LessonEntity.from(lesson));
    List<Tag> savedTags = replaceTags(lesson.id(), lesson.tags());
    return saved.toDomain(savedTags);
  }

  /**
   * レッスンのタグを洗い替えする。
   *
   * <p>本APIは部分更新を行わないため、既存の紐付けを全削除してから渡されたタグを登録する。空リストの場合は削除のみ行い、すべてのタグが外れる。
   *
   * <p>タグ名は全レッスンで共有するマスタなので、{@code tags} への登録は「無ければ作る」とし、そのあとタグ名からIDを引き直して {@code lesson_tags}
   * に紐付ける。{@code createIfAbsent} はIDを返さないため、{@code findByNameIn} で引き直す2段構えになっている。
   *
   * <p>{@code createIfAbsent} はタグ名をソートした順に呼ぶ。未登録の同じタグを2つのリクエストが逆の順番（{@code ["A", "B"]} と {@code
   * ["B", "A"]}）でINSERTすると、一意制約の確認でお互いのトランザクションの完了を待ち合ってデッドロックになるため、どのリクエストでも同じ順番でINSERTするよう揃える。
   *
   * <p>{@code lesson_tags} への登録は、{@link #createLessons} と同じくIDをアプリケーション側で採番してから {@code insertAll}
   * でまとめて登録する。IDが確定していると、JDBCドライバの {@code reWriteBatchedInserts} が複数レコードを1つのINSERT文にまとめられる。
   *
   * @param lessonId レッスンID
   * @param tags 登録するタグのリスト（トリム・重複排除済み。IDは未採番）
   * @return 登録したタグのリスト（DBが採番したIDを含む）
   */
  private List<Tag> replaceTags(UUID lessonId, List<Tag> tags) {
    lessonTagDao.deleteByLessonId(lessonId);

    if (tags.isEmpty()) {
      return List.of();
    }

    List<String> names = tags.stream().map(Tag::name).toList();

    names.stream().sorted().forEach(tagDao::createIfAbsent);

    Map<String, TagEntity> tagByName =
        tagDao.findByNameIn(names).stream()
            .collect(Collectors.toMap(TagEntity::name, tagEntity -> tagEntity));

    jdbcAggregateTemplate.insertAll(
        names.stream()
            .map(
                name ->
                    new LessonTagEntity(UUID.randomUUID(), lessonId, tagByName.get(name).id()))
            .toList());

    return names.stream().map(name -> tagByName.get(name).toDomain()).toList();
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
