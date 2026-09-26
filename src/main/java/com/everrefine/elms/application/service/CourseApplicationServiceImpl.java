package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.CourseCreateCommand;
import com.everrefine.elms.application.command.CourseUpdateCommand;
import com.everrefine.elms.application.dto.CourseDto;
import com.everrefine.elms.application.dto.CoursePageDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.PagerForRequest;
import com.everrefine.elms.domain.model.course.Course;
import com.everrefine.elms.domain.repository.CourseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** コースアプリケーションサービスの実装。 */
@Service
@AllArgsConstructor
public class CourseApplicationServiceImpl implements CourseApplicationService {

  private final CourseRepository courseRepository;

  @Override
  @Transactional(readOnly = true)
  public CourseDto findCourseById(UUID courseId) {
    Course course =
        courseRepository
            .findCourseById(courseId)
            .orElseThrow(
                () -> new ResourceNotFoundException(Course.class, String.valueOf(courseId)));
    return CourseDto.from(course);
  }

  @Override
  @Transactional(readOnly = true)
  public CoursePageDto findCourses(int pageNum, int pageSize) {
    PagerForRequest pagerForRequest = new PagerForRequest(pageNum, pageSize);
    List<Course> courses = courseRepository.findCourses(pagerForRequest);
    int totalSize = courseRepository.countCourses();
    List<CourseDto> courseDtos = courses.stream().map(CourseDto::from).toList();
    return CoursePageDto.from(courseDtos, pageNum, pageSize, totalSize);
  }

  @Override
  @Transactional
  public void createCourse(CourseCreateCommand courseCreateCommand) {
    var courseOrder = courseRepository.issueCourseOrder();
    courseRepository.createCourse(courseCreateCommand.toCourse(courseOrder.value()));
  }

  @Override
  @Transactional
  public void updateCourse(CourseUpdateCommand courseUpdateCommand) {
    Course course =
        courseRepository
            .findCourseById(courseUpdateCommand.id())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        Course.class, String.valueOf(courseUpdateCommand.id())));

    validateCourseOrderNotUsedByOtherCourse(courseUpdateCommand.courseOrder(), course.id());

    courseRepository.updateCourse(courseUpdateCommand.toCourse(course));
  }

  /**
   * コースの表示順が他のコースに使用されていないことを検証する。
   *
   * @param courseOrder 検証対象の表示順
   * @param excludedCourseId 検証から除外するコースID（更新対象のコースID）
   * @throws BadRequestException 他のコースがすでに使用している場合
   */
  private void validateCourseOrderNotUsedByOtherCourse(
      BigDecimal courseOrder, UUID excludedCourseId) {
    if (courseOrder == null) {
      return;
    }

    courseRepository
        .findCourseByCourseOrder(new Order(courseOrder))
        .filter(existingCourse -> !existingCourse.id().equals(excludedCourseId))
        .ifPresent(
            existingCourse -> {
              throw new BadRequestException("この表示順はすでに使用されています");
            });
  }

  @Override
  @Transactional
  public void deleteCourseById(UUID courseId) {
    // コースが存在しなくてもエラーにはしない。
    courseRepository
        .findCourseById(courseId)
        .ifPresent(course -> courseRepository.deleteCourseById(courseId));
  }
}
