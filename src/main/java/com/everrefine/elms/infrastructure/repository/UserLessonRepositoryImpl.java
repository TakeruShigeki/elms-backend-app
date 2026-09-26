package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.UserLesson;
import com.everrefine.elms.domain.repository.UserLessonRepository;
import com.everrefine.elms.infrastructure.dao.UserLessonDao;
import com.everrefine.elms.infrastructure.entity.UserLessonEntity;
import com.everrefine.elms.infrastructure.row.CompletedLessonCountRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

/** {@link UserLessonRepository} の実装。 */
@Repository
@AllArgsConstructor
public class UserLessonRepositoryImpl implements UserLessonRepository {

  private final UserLessonDao userLessonDao;

  @Override
  public Optional<UserLesson> findByUserIdAndLessonId(UUID userId, UUID lessonId) {
    return userLessonDao.findByUserIdAndLessonId(userId, lessonId).map(UserLessonEntity::toDomain);
  }

  @Override
  public void save(UserLesson userLesson) {
    userLessonDao.save(UserLessonEntity.from(userLesson));
  }

  @Override
  public void deleteByUserIdAndLessonId(UUID userId, UUID lessonId) {
    userLessonDao.deleteByUserIdAndLessonId(userId, lessonId);
  }

  @Override
  public int countAllByUserId(UUID userId) {
    return userLessonDao.countAllByUserId(userId);
  }

  @Override
  public Map<UUID, Integer> countByUserIds(List<UUID> userIds) {
    if (userIds.isEmpty()) {
      return Map.of();
    }
    List<CompletedLessonCountRow> counts = userLessonDao.countByUserIds(userIds);
    return counts.stream()
        .collect(
            Collectors.toMap(
                CompletedLessonCountRow::userId, CompletedLessonCountRow::completedCount));
  }

  @Override
  public int countCompletedLessonsByUserIdAndCourseId(UUID userId, UUID courseId) {
    return userLessonDao.countCompletedLessonsByUserIdAndCourseId(userId, courseId);
  }

  @Override
  public Set<UUID> findLessonIdByUserIdAndLessonIdIn(UUID userId, Set<UUID> lessonIds) {
    if (lessonIds.isEmpty()) {
      return Set.of();
    }
    return userLessonDao.findLessonIdByUserIdAndLessonIdIn(userId, lessonIds);
  }
}
