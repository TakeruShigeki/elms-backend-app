package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.CourseCreateCommand;
import com.everrefine.elms.application.command.CourseUpdateCommand;
import com.everrefine.elms.application.command.LessonImportCommand;
import com.everrefine.elms.application.dto.CourseDto;
import com.everrefine.elms.application.dto.CourseLessonsDto;
import com.everrefine.elms.application.dto.CoursePageDto;
import com.everrefine.elms.application.dto.LessonImportResponseDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.service.CourseApplicationService;
import com.everrefine.elms.application.service.LessonApplicationService;
import com.everrefine.elms.presentation.request.CourseCreateRequest;
import com.everrefine.elms.presentation.request.CourseUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** コースに関するコントローラー。 */
@Tag(name = "コース")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

  private final CourseApplicationService courseApplicationService;
  private final LessonApplicationService lessonApplicationService;

  /**
   * ページ情報を指定してコース一覧を取得する。
   *
   * @param pageNum ページ番号
   * @param pageSize 1ページあたりの件数
   * @return コースページDTO
   */
  @Operation(summary = "コース一覧取得", description = "ページ情報を指定してコース一覧を取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません")
  })
  @GetMapping
  public CoursePageDto findCourses(
      @RequestParam(defaultValue = "1") @Positive int pageNum,
      @RequestParam(defaultValue = "10") @Positive int pageSize) {
    return courseApplicationService.findCourses(pageNum, pageSize);
  }

  /**
   * 指定したIDのコースを取得する。
   *
   * @param courseId コースID
   * @return コースDTO
   */
  @Operation(summary = "コース取得", description = "指定したIDのコースを取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "404", description = "コースが見つかりません")
  })
  @GetMapping("/{courseId}")
  public CourseDto findCourseById(@PathVariable UUID courseId) {
    return courseApplicationService.findCourseById(courseId);
  }

  /**
   * 指定したコースに紐づく全レッスンをレッスングループ単位で取得する。
   *
   * @param courseId コースID
   * @return コースに紐づくレッスン一覧DTO
   */
  @Operation(summary = "コースに紐づく全レッスン一覧取得", description = "指定したコースに紐づく全レッスンを取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません")
  })
  @GetMapping("/{courseId}/lessons")
  public CourseLessonsDto findLessonsGroupedByLessonGroup(@PathVariable UUID courseId) {
    return lessonApplicationService.findLessonsGroupedByLessonGroup(courseId);
  }

  /**
   * 全レッスン情報をCSVとして出力する。
   *
   * @return 全レッスンCSVファイルのリソース
   */
  @Operation(summary = "全レッスンCSV出力", description = "全レッスンのcsvを出力します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "出力成功"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/lessons/export")
  public ResponseEntity<Resource> exportAllLessonsCsv() {
    Resource lessonsCsv = lessonApplicationService.exportAllLessonsCsv();
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lessons.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(lessonsCsv);
  }

  /**
   * CSVファイルをアップロードして指定コースのレッスン構成を一括更新する。
   *
   * @param courseId 取り込み対象コースID
   * @param file アップロード対象のCSVファイル
   * @return 取り込んだレッスングループ件数とレッスン件数
   */
  @Operation(summary = "レッスンCSV取込", description = "CSVファイルをアップロードして指定コースのレッスン構成を一括更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取込成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "コースが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping(value = "/{courseId}/lessons/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<LessonImportResponseDto> importLessonsCsv(
      @PathVariable UUID courseId, @RequestParam("file") @NotNull MultipartFile file) {
    LessonImportCommand lessonImportCommand = toLessonImportCommand(courseId, file);
    LessonImportResponseDto response =
        lessonApplicationService.importLessonsCsv(lessonImportCommand);
    return ResponseEntity.ok(response);
  }

  private LessonImportCommand toLessonImportCommand(UUID courseId, MultipartFile file) {
    try {
      return LessonImportCommand.from(courseId, file.getOriginalFilename(), file.getBytes());
    } catch (IOException e) {
      throw new BadRequestException("CSVファイルの読み込みに失敗しました", e);
    }
  }

  /**
   * 新規コースを作成する。
   *
   * @param courseCreateRequest コース作成リクエスト
   */
  @Operation(summary = "コース作成", description = "新規コースを作成します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "作成成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping
  public void createCourse(@RequestBody @Valid CourseCreateRequest courseCreateRequest) {
    CourseCreateCommand courseCreateCommand = courseCreateRequest.toCommand();
    courseApplicationService.createCourse(courseCreateCommand);
  }

  /**
   * 指定したIDのコース情報を更新する。
   *
   * @param courseId 更新対象のコースID
   * @param courseUpdateRequest コース更新リクエスト
   */
  @Operation(summary = "コース更新", description = "指定したIDのコース情報を更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "コースが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{courseId}")
  public void updateCourse(
      @PathVariable UUID courseId, @RequestBody @Valid CourseUpdateRequest courseUpdateRequest) {
    CourseUpdateCommand courseUpdateCommand = courseUpdateRequest.toCommand(courseId);
    courseApplicationService.updateCourse(courseUpdateCommand);
  }

  /**
   * 指定したIDのコースを削除する。
   *
   * @param courseId 削除対象のコースID
   */
  @Operation(summary = "コース削除", description = "指定したIDのコースを削除します")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "削除成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{courseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourseById(@PathVariable UUID courseId) {
    courseApplicationService.deleteCourseById(courseId);
  }
}
