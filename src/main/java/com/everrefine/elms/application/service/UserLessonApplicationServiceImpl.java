package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.UserLessonCompletionStatusUpdateCommand;
import com.everrefine.elms.application.dto.UserLessonDetailDto;
import com.everrefine.elms.application.dto.UserLessonGroupDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.UserLesson;
import com.everrefine.elms.domain.model.course.Course;
import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import com.everrefine.elms.domain.model.lesson.LessonInGroup;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.repository.CourseRepository;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.domain.repository.UserLessonRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ユーザーレッスンアプリケーションサービスの実装。 */
@Service
@AllArgsConstructor
public class UserLessonApplicationServiceImpl implements UserLessonApplicationService {

  private final LessonRepository lessonRepository;
  private final UserRepository userRepository;
  private final UserLessonRepository userLessonRepository;
  private final CourseRepository courseRepository;

  @Override
  @Transactional(readOnly = true)
  public UserLessonDetailDto findUserLessonDetail(
      UUID userId, UUID courseId, UUID lessonGroupId, UUID lessonId) {
    Lesson lesson =
        findLessonBelongingToCourseAndLessonGroupOrThrow(lessonId, courseId, lessonGroupId);
    boolean isLessonCompleted =
        userLessonRepository.findByUserIdAndLessonId(userId, lessonId).isPresent();
    return UserLessonDetailDto.from(lesson, isLessonCompleted);
  }

  @Override
  @Transactional
  public void updateUserLesson(
      UUID courseId,
      UUID lessonGroupId,
      UserLessonCompletionStatusUpdateCommand userLessonCompletionStatusUpdateCommand) {
    // データが存在することのチェックする。
    findLessonBelongingToCourseAndLessonGroupOrThrow(
        userLessonCompletionStatusUpdateCommand.lessonId(), courseId, lessonGroupId);
    userRepository
        .findUserById(userLessonCompletionStatusUpdateCommand.userId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    User.class, userLessonCompletionStatusUpdateCommand.userId().toString()));

    // ユーザーのレッスン完了状態を更新する。
    if (userLessonCompletionStatusUpdateCommand.isLessonCompleted()) {
      userLessonRepository
          .findByUserIdAndLessonId(
              userLessonCompletionStatusUpdateCommand.userId(),
              userLessonCompletionStatusUpdateCommand.lessonId())
          .ifPresentOrElse(
              currentUserLesson -> {
                UserLesson updatedUserLesson = currentUserLesson.update();
                userLessonRepository.save(updatedUserLesson);
              },
              () -> {
                UserLesson createdUserLesson =
                    userLessonCompletionStatusUpdateCommand.toNewUserLesson();
                userLessonRepository.save(createdUserLesson);
              });
    } else {
      userLessonRepository.deleteByUserIdAndLessonId(
          userLessonCompletionStatusUpdateCommand.userId(),
          userLessonCompletionStatusUpdateCommand.lessonId());
    }
  }

  /**
   * コースとレッスングループに属するレッスンを取得する。存在しない場合は例外をスローする。
   *
   * @param lessonId レッスンID
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @return レッスンエンティティ
   */
  private Lesson findLessonBelongingToCourseAndLessonGroupOrThrow(
      UUID lessonId, UUID courseId, UUID lessonGroupId) {
    Lesson lesson = findLessonOrThrow(lessonId);
    if (!lesson.courseId().equals(courseId) || !lesson.lessonGroupId().equals(lessonGroupId)) {
      throw new ResourceNotFoundException(Lesson.class, String.valueOf(lessonId));
    }
    return lesson;
  }

  /**
   * IDでレッスンを取得する。存在しない場合は例外をスローする。
   *
   * @param lessonId レッスンID
   * @return レッスンエンティティ
   */
  private Lesson findLessonOrThrow(UUID lessonId) {
    return lessonRepository
        .findById(lessonId)
        .orElseThrow(() -> new ResourceNotFoundException(Lesson.class, String.valueOf(lessonId)));
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserLessonGroupDto> findUserLessons(UUID userId, UUID courseId) {
    userRepository
        .findUserById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(User.class, String.valueOf(userId)));
    courseRepository
        .findCourseById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException(Course.class, String.valueOf(courseId)));

    List<LessonGroupWithLessons> lessonGroups =
        lessonRepository.findLessonsGroupedByLessonGroup(courseId);

    Set<UUID> lessonIds =
        lessonGroups.stream()
            .flatMap(group -> group.lessons().stream())
            .map(LessonInGroup::id)
            .collect(Collectors.toSet());

    Set<UUID> completedLessonIds =
        lessonIds.isEmpty()
            ? Collections.emptySet()
            : userLessonRepository.findLessonIdByUserIdAndLessonIdIn(userId, lessonIds);

    return lessonGroups.stream()
        .map(group -> UserLessonGroupDto.from(group, completedLessonIds))
        .toList();
  }
}
