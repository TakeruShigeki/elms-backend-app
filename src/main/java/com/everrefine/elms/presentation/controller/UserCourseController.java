package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.UserLessonCompletionStatusUpdateCommand;
import com.everrefine.elms.application.dto.UserCourseDto;
import com.everrefine.elms.application.dto.UserLessonDetailDto;
import com.everrefine.elms.application.dto.UserLessonGroupDto;
import com.everrefine.elms.application.service.UserCourseApplicationService;
import com.everrefine.elms.application.service.UserLessonApplicationService;
import com.everrefine.elms.presentation.request.UserLessonCompletionStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 該当ユーザーのコース受講状況に関するコントローラー。 */
@Tag(name = "該当ユーザーのコース受講状況")
@RestController
@RequestMapping("/api/users/{userId}/courses")
@RequiredArgsConstructor
public class UserCourseController {

  private final UserCourseApplicationService userCourseApplicationService;
  private final UserLessonApplicationService userLessonApplicationService;

  /**
   * 指定したユーザーのコース一覧を取得する。
   *
   * @param userId ユーザーID
   * @return ユーザーコースDTOリスト
   */
  @Operation(summary = "該当ユーザーのコース一覧取得", description = "指定したユーザーのコース一覧を取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "404", description = "ユーザーが見つかりません")
  })
  @GetMapping
  public List<UserCourseDto> findUserCourses(@PathVariable UUID userId) {
    return userCourseApplicationService.findUserCourses(userId);
  }

  /**
   * 指定したユーザー・コースに紐づくレッスン（完了状態含む）一覧を取得する。
   *
   * @param userId ユーザーID
   * @param courseId コースID
   * @return ユーザーレッスングループDTOリスト
   */
  @Operation(summary = "レッスン一覧取得", description = "該当ユーザーに紐づく該当コースのレッスン（完了状態含む）一覧を取得します。")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "404", description = "レッスンまたはユーザーが見つかりません")
  })
  @GetMapping("/{courseId}/lessons")
  public List<UserLessonGroupDto> findUserLessons(
      @PathVariable UUID userId, @PathVariable UUID courseId) {
    return userLessonApplicationService.findUserLessons(userId, courseId);
  }

  /**
   * 指定したユーザーのレッスン詳細（完了状態含む）を取得する。
   *
   * @param userId ユーザーID
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonId レッスンID
   * @return ユーザーレッスン詳細DTO
   */
  @Operation(summary = "受講レッスン詳細取得", description = "指定したユーザーのレッスン詳細（完了状態含む）を取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "404", description = "レッスンまたはユーザーが見つかりません")
  })
  @GetMapping("/{courseId}/lesson-groups/{lessonGroupId}/lessons/{lessonId}")
  public UserLessonDetailDto findUserLessonDetail(
      @PathVariable UUID userId,
      @PathVariable UUID courseId,
      @PathVariable UUID lessonGroupId,
      @PathVariable UUID lessonId) {
    return userLessonApplicationService.findUserLessonDetail(
        userId, courseId, lessonGroupId, lessonId);
  }

  /**
   * 指定したユーザーのレッスン完了状態を更新する。
   *
   * @param userId ユーザーID
   * @param courseId コースID
   * @param lessonGroupId レッスングループID
   * @param lessonId 対象のレッスンID
   * @param userLessonCompletionStatusUpdateRequest レッスン完了状態更新リクエスト
   */
  @Operation(summary = "レッスン完了状態更新", description = "指定したユーザーのレッスン完了状態を更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "404", description = "レッスンまたはユーザーが見つかりません")
  })
  @PutMapping("/{courseId}/lesson-groups/{lessonGroupId}/lessons/{lessonId}/completion")
  public void updateUserLessonCompletionStatus(
      @PathVariable UUID userId,
      @PathVariable UUID courseId,
      @PathVariable UUID lessonGroupId,
      @PathVariable UUID lessonId,
      @RequestBody
          UserLessonCompletionStatusUpdateRequest userLessonCompletionStatusUpdateRequest) {
    UserLessonCompletionStatusUpdateCommand userLessonCompletionStatusUpdateCommand =
        userLessonCompletionStatusUpdateRequest.toCommand(userId, lessonId);
    userLessonApplicationService.updateUserLesson(
        courseId, lessonGroupId, userLessonCompletionStatusUpdateCommand);
  }
}
