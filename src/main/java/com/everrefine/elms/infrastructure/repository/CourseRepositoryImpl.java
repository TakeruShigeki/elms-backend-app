package com.everrefine.elms.infrastructure.repository;

import static com.everrefine.elms.domain.model.Order.getFirst;

import com.everrefine.elms.domain.model.Order;
import com.everrefine.elms.domain.model.PagerForRequest;
import com.everrefine.elms.domain.model.course.Course;
import com.everrefine.elms.domain.repository.CourseRepository;
import com.everrefine.elms.infrastructure.dao.CourseDao;
import com.everrefine.elms.infrastructure.entity.course.CourseEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

/** {@link CourseRepository} の実装。 */
@Repository
@AllArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {

  private final CourseDao courseDao;

  @Override
  public void updateCourse(Course course) {
    courseDao.save(CourseEntity.from(course));
  }

  @Override
  public void createCourse(Course course) {
    courseDao.save(CourseEntity.from(course));
  }

  @Override
  public void deleteCourseById(UUID id) {
    courseDao.deleteById(id);
  }

  @Override
  public Optional<Course> findCourseById(UUID id) {
    return courseDao.findById(id).map(CourseEntity::toDomain);
  }

  @Override
  public Optional<Course> findCourseByCourseOrder(Order courseOrder) {
    return courseDao.findByCourseOrder(courseOrder.value()).map(CourseEntity::toDomain);
  }

  @Override
  public List<Course> findCourses(PagerForRequest pagerForRequest) {
    return courseDao
        .findCoursesWithPagination(pagerForRequest.pageSize(), pagerForRequest.getOffset())
        .stream()
        .map(CourseEntity::toDomain)
        .toList();
  }

  @Override
  public int countCourses() {
    return courseDao.countAllCourses();
  }

  @Override
  public Order issueCourseOrder() {
    return courseDao
        .findTop1ByOrderByCourseOrderDesc()
        .map(CourseEntity::toDomain)
        .map(course -> course.courseOrder().getNext())
        .orElse(getFirst());
  }

  @Override
  public List<String> findByThumbnailUrlStartingWith(String prefix) {
    return courseDao.findByThumbnailUrlStartingWith(prefix);
  }
}
