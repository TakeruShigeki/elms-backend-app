package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.LessonCreateCommand;
import com.everrefine.elms.application.command.LessonImportCommand;
import com.everrefine.elms.application.command.LessonImportRowCommand;
import com.everrefine.elms.application.command.LessonOrderUpdateCommand;
import com.everrefine.elms.application.command.LessonSearchCommand;
import com.everrefine.elms.application.command.LessonUpdateCommand;
import com.everrefine.elms.application.dto.*;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.course.Course;
import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonGroup;
import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import com.everrefine.elms.domain.model.lesson.LessonWithCourseAndLessonGroup;
import com.everrefine.elms.domain.repository.CourseRepository;
import com.everrefine.elms.domain.repository.LessonGroupRepository;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.domain.service.LessonDomainService;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** レッスンアプリケーションサービスの実装。 */
@Service
@AllArgsConstructor
public class LessonApplicationServiceImpl implements LessonApplicationService {

  private final LessonRepository lessonRepository;
  private final LessonGroupRepository lessonGroupRepository;
  private final CourseRepository courseRepository;
  private final LessonDomainService lessonDomainService;

  /**
   * CSV出力用に値をエスケープする。
   *
   * <p>値にカンマ、ダブルクォーテーション、改行があった場合のエスケープ処理。
   *
   * @param value エスケープ対象の文字列
   * @return エスケープ後の文字列
   */
  private String escape(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",")
        || value.contains("\"")
        || value.contains("\n")
        || value.contains("\r")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  @Override
  @Transactional(readOnly = true)
  public LessonDto findLessonById(UUID courseId, UUID lessonGroupId, UUID lessonId) {
    Lesson lesson = findLessonBelongingToCourseAndGroupOrThrow(lessonId, courseId, lessonGroupId);
    return LessonDto.from(lesson);
  }

  @Override
  @Transactional(readOnly = true)
  public LessonPageDto findLessons(LessonSearchCommand lessonSearchCommand) {
    List<Lesson> lessons = lessonRepository.findLessons(lessonSearchCommand.toCriteria());
    int totalSize = lessonRepository.countLessons(lessonSearchCommand.toCriteria());

    List<LessonDto> lessonDtos = lessons.stream().map(LessonDto::from).toList();

    return LessonPageDto.from(
        lessonDtos, lessonSearchCommand.pageNum(), lessonSearchCommand.pageSize(), totalSize);
  }

  /**
   * コースとレッスングループに属するレッスンを取得する。存在しない場合は例外をスローする。
   *
   * @param lessonId レッスンID
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @return レッスンエンティティ
   */
  private Lesson findLessonBelongingToCourseAndGroupOrThrow(
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
  public CourseLessonsDto findLessonsGroupedByLessonGroup(UUID courseId) {
    List<LessonGroupWithLessons> lessonGroups =
        lessonRepository.findLessonsGroupedByLessonGroup(courseId);
    List<LessonGroupDto> lessonGroupDtos = lessonGroups.stream().map(LessonGroupDto::from).toList();
    return new CourseLessonsDto(courseId, lessonGroupDtos);
  }

  @Override
  @Transactional
  public LessonDto createLesson(LessonCreateCommand lessonCreateCommand) {
    throwExceptionIfLessonGroupNotBelongsToCourse(
        lessonCreateCommand.lessonGroupId(), lessonCreateCommand.courseId());

    BigDecimal lessonOrder =
        lessonDomainService.issueLessonOrder(lessonCreateCommand.lessonGroupId());
    Lesson createdLesson = lessonRepository.createLesson(lessonCreateCommand.toLesson(lessonOrder));
    return LessonDto.from(createdLesson);
  }

  /**
   * レッスングループが存在しない、または指定したコースに属さない場合に例外をスローする。
   *
   * <p>検証しないまま登録すると外部キー制約違反となり、クライアント起因の誤りが500として返ってしまう。
   *
   * @param lessonGroupId レッスングループID
   * @param courseId コースID
   */
  private void throwExceptionIfLessonGroupNotBelongsToCourse(UUID lessonGroupId, UUID courseId) {
    LessonGroup lessonGroup =
        lessonGroupRepository
            .findLessonGroupById(lessonGroupId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        LessonGroup.class, String.valueOf(lessonGroupId)));

    if (!lessonGroup.courseId().equals(courseId)) {
      throw new ResourceNotFoundException(LessonGroup.class, String.valueOf(lessonGroupId));
    }
  }

  @Override
  @Transactional
  public LessonDto updateLesson(LessonUpdateCommand lessonUpdateCommand) {
    Lesson currentLesson = findLessonOrThrow(lessonUpdateCommand.id());
    Lesson updatedLesson =
        lessonRepository.updateLesson(lessonUpdateCommand.toLesson(currentLesson));
    return LessonDto.from(updatedLesson);
  }

  @Override
  @Transactional
  public void deleteLessonById(UUID lessonId) {
    lessonRepository
        .findById(lessonId)
        .ifPresent(lesson -> lessonRepository.deleteLessonById(lessonId));
  }

  /**
   * レッスンIDに対応するレッスン順序を返す。IDがnullの場合はnullを返す。
   *
   * @param lessonId レッスンID（nullの場合はnullを返す）
   * @param lessonIdAndLessonMap レッスンIDとレッスンのMap
   * @return レッスン順序（lessonIdがnullの場合はnull）
   */
  private BigDecimal resolveLessonOrderOrNull(
      UUID lessonId, Map<UUID, Lesson> lessonIdAndLessonMap) {
    if (lessonId == null) {
      return null;
    }

    Lesson lesson = lessonIdAndLessonMap.get(lessonId);
    if (lesson == null) {
      throw new ResourceNotFoundException(Lesson.class, String.valueOf(lessonId));
    }

    return lesson.lessonOrder().value();
  }

  @Override
  @Transactional
  public LessonDto updateLessonOrder(LessonOrderUpdateCommand lessonOrderUpdateCommand) {
    UUID targetLessonId = lessonOrderUpdateCommand.lessonId();
    UUID precedingLessonId = lessonOrderUpdateCommand.precedingLessonId();
    UUID followingLessonId = lessonOrderUpdateCommand.followingLessonId();

    List<UUID> lessonIds = new ArrayList<>();
    lessonIds.add(targetLessonId);
    if (precedingLessonId != null && !lessonIds.contains(precedingLessonId)) {
      lessonIds.add(precedingLessonId);
    }
    if (followingLessonId != null && !lessonIds.contains(followingLessonId)) {
      lessonIds.add(followingLessonId);
    }

    Map<UUID, Lesson> lessonIdAndLessonMap =
        lessonRepository.findByIdIn(lessonIds).stream()
            .collect(Collectors.toMap(Lesson::id, Function.identity()));

    Lesson targetLesson = lessonIdAndLessonMap.get(targetLessonId);
    if (targetLesson == null) {
      throw new ResourceNotFoundException(Lesson.class, String.valueOf(targetLessonId));
    }

    BigDecimal precedingOrder = resolveLessonOrderOrNull(precedingLessonId, lessonIdAndLessonMap);

    BigDecimal followingOrder = resolveLessonOrderOrNull(followingLessonId, lessonIdAndLessonMap);

    if (precedingOrder == null && followingOrder == null) {
      throw new BadRequestException("移動先の前後いずれかのレッスンを指定してください");
    }

    BigDecimal newOrder = lessonDomainService.calculateNewOrder(precedingOrder, followingOrder);

    if (!lessonDomainService.hasRoomForNewOrder(newOrder, precedingOrder, followingOrder)) {
      throw new BadRequestException("この位置にはこれ以上移動できません。前後のレッスンの並び順を空けてから再度実行してください");
    }

    Lesson updatedLesson = targetLesson.updateOrder(newOrder);
    Lesson savedLesson = lessonRepository.updateLesson(updatedLesson);

    return LessonDto.from(savedLesson);
  }

  /**
   * CSVファイルをアップロードして指定コースのレッスン構成を一括更新する。
   *
   * @param lessonImportCommand レッスン取込用Command
   * @return 取込したレッスングループ件数とレッスン件数
   */
  @Override
  @Transactional
  public LessonImportResponseDto importLessonsCsv(LessonImportCommand lessonImportCommand) {
    UUID courseId = lessonImportCommand.courseId();
    throwExceptionIfCourseNotExists(courseId);

    Map<String, List<LessonImportRowCommand>> rowsByLessonGroupTitle =
        lessonImportCommand.getRowsByLessonGroupTitle();

    lessonRepository.deleteLessonsByCourseId(courseId);
    lessonGroupRepository.deleteLessonGroupsByCourseId(courseId);

    importLessonGroupsAndLessons(courseId, rowsByLessonGroupTitle);

    return new LessonImportResponseDto(
        lessonImportCommand.getImportedLessonGroupCount(),
        lessonImportCommand.getImportedLessonCount());
  }

  /**
   * レッスングループとレッスンを一括登録する。
   *
   * <p>レッスングループのIDはアプリケーション側で採番済みのため、登録前に子レッスンへ紐づけられる。
   * これによりレッスングループを1件ずつINSERTする必要がなく、親子それぞれ1回の一括登録で完結する。
   *
   * @param courseId コースID
   * @param rowsByLessonGroupTitle レッスングループタイトルごとのCSV行
   */
  private void importLessonGroupsAndLessons(
      UUID courseId, Map<String, List<LessonImportRowCommand>> rowsByLessonGroupTitle) {
    List<LessonGroup> lessonGroups = new ArrayList<>();
    List<Lesson> lessons = new ArrayList<>();
    int lessonGroupIndex = 0;
    for (List<LessonImportRowCommand> lessonGroupRows : rowsByLessonGroupTitle.values()) {
      LessonGroup lessonGroup = toLessonGroup(courseId, lessonGroupRows, lessonGroupIndex);
      lessonGroups.add(lessonGroup);
      lessons.addAll(toLessons(courseId, lessonGroup.id(), lessonGroupRows));
      lessonGroupIndex++;
    }

    lessonGroupRepository.createLessonGroups(lessonGroups);
    lessonRepository.createLessons(lessons);
  }

  private LessonGroup toLessonGroup(
      UUID courseId, List<LessonImportRowCommand> lessonGroupRows, int lessonGroupIndex) {
    LessonImportRowCommand firstRow = lessonGroupRows.getFirst();
    return firstRow.toLessonGroup(courseId, LessonImportCommand.calculateOrder(lessonGroupIndex));
  }

  private List<Lesson> toLessons(
      UUID courseId, UUID lessonGroupId, List<LessonImportRowCommand> lessonGroupRows) {
    List<Lesson> lessons = new ArrayList<>();
    int lessonIndex = 0;
    for (LessonImportRowCommand row : lessonGroupRows) {
      lessons.add(
          row.toLesson(lessonGroupId, courseId, LessonImportCommand.calculateOrder(lessonIndex)));
      lessonIndex++;
    }
    return lessons;
  }

  private void throwExceptionIfCourseNotExists(UUID courseId) {
    courseRepository
        .findCourseById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException(Course.class, String.valueOf(courseId)));
  }

  @Transactional(readOnly = true)
  @Override
  public Resource exportAllLessonsCsv() {

    String[] header = {
      "コースID", "コース名", "レッスングループID", "レッスングループ名", "レッスンID", "レッスンタイトル", "レッスンの動画URL"
    };

    List<LessonWithCourseAndLessonGroup> allLessons = lessonRepository.findAllLessons();

    if (allLessons.isEmpty()) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (PrintWriter file =
          new PrintWriter(
              new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8)))) {
        file.print('\uFEFF');
        file.println(String.join(",", header));
      } catch (Exception e) {
        throw new RuntimeException("CSVファイルの作成に失敗しました", e);
      }
      return new ByteArrayResource(baos.toByteArray());
    }

    List<LessonWithCourseAndLessonGroupDto> dtos =
        allLessons.stream().map(LessonWithCourseAndLessonGroupDto::from).toList();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (
    // 書き込むファイルを作成
    PrintWriter file =
        new PrintWriter(
            new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))); ) {
      // Excelで開いたときの文字化けを防ぐ
      file.print('\uFEFF');

      // ヘッダーをセットする
      file.println(String.join(",", header));

      // 中身をセットする ( 1レッスンごとに改行 )
      for (LessonWithCourseAndLessonGroupDto dto : dtos) {
        String[] lessons = {
          escape(String.valueOf(dto.courseId())),
          escape(dto.courseTitle()),
          escape(String.valueOf(dto.lessonGroupId())),
          escape(dto.lessonGroupTitle()),
          escape(String.valueOf(dto.lessonId())),
          escape(dto.lessonTitle()),
          escape(dto.videoUrl())
        };
        file.println(String.join(",", lessons));
      }
    } catch (Exception e) {
      throw new RuntimeException("CSVファイルの作成に失敗しました", e);
    }
    return new ByteArrayResource(baos.toByteArray());
  }
}
