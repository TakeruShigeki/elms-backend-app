package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.LessonCreateCommand;
import com.everrefine.elms.application.command.LessonOrderUpdateCommand;
import com.everrefine.elms.application.command.LessonUpdateCommand;
import com.everrefine.elms.application.dto.LessonDto;
import com.everrefine.elms.application.service.LessonApplicationService;
import com.everrefine.elms.presentation.request.LessonCreateRequest;
import com.everrefine.elms.presentation.request.LessonOrderUpdateRequest;
import com.everrefine.elms.presentation.request.LessonUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** レッスンに関するコントローラー。 */
@Tag(name = "レッスン")
@RestController
@RequestMapping("/api/courses/{courseId}/lesson-groups/{lessonGroupId}/lessons")
@RequiredArgsConstructor
public class LessonController {

  private final LessonApplicationService lessonApplicationService;

  /**
   * 指定したIDのレッスンを取得する（管理者向け・完了状態は含まない）。
   *
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonId レッスンID
   * @return レッスンDTO
   */
  @Operation(summary = "レッスン取得", description = "指定したIDのレッスンを取得します（管理者向け・完了状態は含まない）")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "レッスンが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/{lessonId}")
  public LessonDto findLessonById(
      @PathVariable UUID courseId, @PathVariable UUID lessonGroupId, @PathVariable UUID lessonId) {
    return lessonApplicationService.findLessonById(courseId, lessonGroupId, lessonId);
  }

  /**
   * 指定したコース・レッスングループに新規レッスンを作成する。
   *
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonCreateRequest レッスン作成リクエスト
   * @return 作成したレッスンDTO
   */
  @Operation(summary = "レッスン作成", description = "指定したコース・レッスングループに新規レッスンを作成します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "作成成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー（タイトルは必須）"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "レッスングループが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping
  public LessonDto createLesson(
      @PathVariable UUID courseId,
      @PathVariable UUID lessonGroupId,
      @RequestBody @Valid LessonCreateRequest lessonCreateRequest) {
    LessonCreateCommand lessonCreateCommand =
        lessonCreateRequest.toCommand(courseId, lessonGroupId);
    return lessonApplicationService.createLesson(lessonCreateCommand);
  }

  /**
   * 指定したIDのレッスン情報を更新する。
   *
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonId 更新対象のレッスンID
   * @param lessonUpdateRequest レッスン更新リクエスト
   * @return 更新後のレッスンDTO
   */
  @Operation(summary = "レッスン更新", description = "指定したIDのレッスン情報を更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー（タイトルは必須）"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "レッスンが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{lessonId}")
  public LessonDto updateLesson(
      @PathVariable UUID courseId,
      @PathVariable UUID lessonGroupId,
      @PathVariable UUID lessonId,
      @RequestBody @Valid LessonUpdateRequest lessonUpdateRequest) {
    LessonUpdateCommand lessonUpdateCommand = lessonUpdateRequest.toCommand(lessonId);
    return lessonApplicationService.updateLesson(lessonUpdateCommand);
  }

  /**
   * 指定したレッスンの表示順を変更する。
   *
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonId 並び順を変更するレッスンID
   * @param lessonOrderUpdateRequest レッスン並び順更新リクエスト
   * @return 更新後のレッスンDTO
   */
  @Operation(summary = "レッスン並び順更新", description = "指定したレッスンの表示順を変更します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "レッスンが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{lessonId}/order")
  public LessonDto updateLessonOrder(
      @PathVariable UUID courseId,
      @PathVariable UUID lessonGroupId,
      @PathVariable UUID lessonId,
      @RequestBody @Valid LessonOrderUpdateRequest lessonOrderUpdateRequest) {
    LessonOrderUpdateCommand lessonOrderUpdateCommand =
        lessonOrderUpdateRequest.toCommand(lessonId);
    return lessonApplicationService.updateLessonOrder(lessonOrderUpdateCommand);
  }

  /**
   * 指定したIDのレッスンを削除する。
   *
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonId 削除対象のレッスンID
   */
  @Operation(summary = "レッスン削除", description = "指定したIDのレッスンを削除します")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "削除成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{lessonId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteLessonById(
      @PathVariable UUID courseId, @PathVariable UUID lessonGroupId, @PathVariable UUID lessonId) {
    lessonApplicationService.deleteLessonById(lessonId);
  }
}
