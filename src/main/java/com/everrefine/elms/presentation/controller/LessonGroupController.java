package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.LessonGroupCreateCommand;
import com.everrefine.elms.application.command.LessonGroupUpdateCommand;
import com.everrefine.elms.application.dto.LessonGroupDto;
import com.everrefine.elms.application.service.LessonGroupApplicationService;
import com.everrefine.elms.presentation.request.LessonGroupCreateRequest;
import com.everrefine.elms.presentation.request.LessonGroupUpdateRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** レッスングループに関するコントローラー。 */
@Tag(name = "レッスングループ")
@RestController
@RequestMapping("/api/courses/{courseId}/lesson-groups")
@RequiredArgsConstructor
public class LessonGroupController {

  private final LessonGroupApplicationService lessonGroupApplicationService;

  /**
   * 指定したコースに新規レッスングループを作成する。
   *
   * @param courseId コースID
   * @param lessonGroupCreateRequest レッスングループ作成リクエスト
   * @return 作成したレッスングループDTO
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @Operation(summary = "レッスングループ作成", description = "指定したコースに新規レッスングループを作成します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "作成成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー（タイトルは必須・100文字以内）"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "コースが見つかりません")
  })
  @PostMapping
  public LessonGroupDto createLessonGroup(
      @PathVariable UUID courseId,
      @RequestBody @Valid LessonGroupCreateRequest lessonGroupCreateRequest) {
    LessonGroupCreateCommand lessonGroupCreateCommand =
        lessonGroupCreateRequest.toCommand(courseId);
    return lessonGroupApplicationService.createLessonGroup(lessonGroupCreateCommand);
  }

  /**
   * 指定したIDのレッスングループ情報を更新する。
   *
   * @param courseId コースID
   * @param lessonGroupId 更新対象のレッスングループID
   * @param lessonGroupUpdateRequest レッスングループ更新リクエスト
   * @return 更新後のレッスングループDTO
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @Operation(summary = "レッスングループ更新", description = "指定したIDのレッスングループ情報を更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー（タイトルは必須・100文字以内）"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "レッスングループが見つかりません")
  })
  @PutMapping("/{lessonGroupId}")
  public LessonGroupDto updateLessonGroup(
      @PathVariable UUID courseId,
      @PathVariable UUID lessonGroupId,
      @RequestBody @Valid LessonGroupUpdateRequest lessonGroupUpdateRequest) {
    LessonGroupUpdateCommand lessonGroupUpdateCommand =
        lessonGroupUpdateRequest.toCommand(lessonGroupId);
    return lessonGroupApplicationService.updateLessonGroup(lessonGroupUpdateCommand);
  }

  /**
   * 指定したIDのレッスングループを削除する。
   *
   * @param courseId コースID
   * @param lessonGroupId 削除対象のレッスングループID
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @Operation(summary = "レッスングループ削除", description = "指定したIDのレッスングループを削除します")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "削除成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @DeleteMapping("/{lessonGroupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteLessonGroup(@PathVariable UUID courseId, @PathVariable UUID lessonGroupId) {
    lessonGroupApplicationService.deleteLessonGroupById(lessonGroupId);
  }
}
