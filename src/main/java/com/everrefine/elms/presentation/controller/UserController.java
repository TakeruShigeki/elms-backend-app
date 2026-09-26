package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.PasswordUpdateCommand;
import com.everrefine.elms.application.command.UserCreateCommand;
import com.everrefine.elms.application.command.UserImportCommand;
import com.everrefine.elms.application.command.UserSearchCommand;
import com.everrefine.elms.application.command.UserUpdateCommand;
import com.everrefine.elms.application.dto.UserDto;
import com.everrefine.elms.application.dto.UserImportResponseDto;
import com.everrefine.elms.application.dto.UserPageDto;
import com.everrefine.elms.application.service.UserApplicationService;
import com.everrefine.elms.presentation.request.PasswordUpdateRequest;
import com.everrefine.elms.presentation.request.UserCreateRequest;
import com.everrefine.elms.presentation.request.UserSearchRequest;
import com.everrefine.elms.presentation.request.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

/** ユーザーに関するコントローラー。 */
@Tag(name = "ユーザー")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserApplicationService userApplicationService;

  /**
   * 検索条件・ページ情報を指定してユーザー一覧を取得する。
   *
   * @param userSearchRequest ユーザー検索リクエスト（検索条件・ページ情報）
   * @return ユーザーページDTO
   */
  @Operation(summary = "ユーザー一覧取得", description = "検索条件・ページ情報を指定してユーザー一覧を取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping
  public UserPageDto findUsers(@Valid UserSearchRequest userSearchRequest) {
    UserSearchCommand userSearchCommand = userSearchRequest.toCommand();
    return userApplicationService.findUsers(userSearchCommand);
  }

  /**
   * 指定したIDのユーザーを取得する。
   *
   * @param userId ユーザーID
   * @return ユーザーDTO
   */
  @Operation(summary = "ユーザー取得", description = "指定したIDのユーザーを取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "アクセス権限がありません"),
    @ApiResponse(responseCode = "404", description = "ユーザーが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN') or #userId.toString() == authentication.name")
  @GetMapping("/{userId}")
  public UserDto findUserById(@PathVariable UUID userId) {
    return userApplicationService.findUserById(userId);
  }

  /**
   * 全ユーザー情報をCSVとして出力する。
   *
   * @return ユーザーCSVファイルのリソース
   */
  @Operation(summary = "ユーザーCSV出力", description = "全ユーザー情報をCSVとして出力します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "出力成功"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/export")
  public ResponseEntity<Resource> exportUsersCsv() {
    Resource usersCsv = userApplicationService.exportUsersCsv();
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(usersCsv);
  }

  /**
   * 新規ユーザーを作成する。
   *
   * @param userCreateRequest ユーザー作成リクエスト
   */
  @Operation(summary = "ユーザー作成", description = "新規ユーザーを作成します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "作成成功"),
    @ApiResponse(responseCode = "400", description = "パスワードと確認用パスワードが一致しません"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping
  public void createUser(@RequestBody @Valid UserCreateRequest userCreateRequest) {
    UserCreateCommand userCreateCommand = userCreateRequest.toCommand();
    userApplicationService.createUser(userCreateCommand);
  }

  /**
   * CSVファイルをアップロードしてユーザー情報を一括更新する。
   *
   * @param file アップロード対象のCSVファイル
   * @param authentication 認証情報
   * @return 取込した件数
   */
  @Operation(summary = "ユーザーCSV取込", description = "CSVファイルをアップロードしてユーザー情報を一括更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取込成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "現在ログイン中のユーザーが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/import")
  public ResponseEntity<UserImportResponseDto> importUsersCsv(
      @RequestParam("file") @NotNull MultipartFile file, Authentication authentication) {
    UUID currentUserId = UUID.fromString(authentication.getName());
    UserImportCommand userImportCommand = UserImportCommand.from(file, currentUserId);
    UserImportResponseDto response = userApplicationService.importUsersCsv(userImportCommand);
    return ResponseEntity.ok(response);
  }

  /**
   * 指定したIDのユーザー情報を更新する。
   *
   * @param userId 更新対象のユーザーID
   * @param userUpdateRequest ユーザー更新リクエスト
   */
  @Operation(summary = "ユーザー更新", description = "指定したIDのユーザー情報を更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "アクセス権限がありません"),
    @ApiResponse(responseCode = "404", description = "ユーザーが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN') or #userId.toString() == authentication.name")
  @PutMapping("/{userId}")
  public void updateUser(
      @PathVariable UUID userId, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
    UserUpdateCommand userUpdateCommand = userUpdateRequest.toCommand(userId);
    userApplicationService.updateUser(userUpdateCommand);
  }

  /**
   * ユーザーのパスワードを変更する。
   *
   * @param passwordUpdateRequest パスワード変更リクエスト
   */
  @Operation(summary = "パスワード変更", description = "ユーザーのパスワードを変更します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "変更成功"),
    @ApiResponse(responseCode = "400", description = "パスワードが一致しません"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "404", description = "ユーザーが存在しません")
  })
  @PutMapping("/password")
  public void updatePassword(@RequestBody @Valid PasswordUpdateRequest passwordUpdateRequest) {
    PasswordUpdateCommand passwordUpdateCommand = passwordUpdateRequest.toCommand();
    userApplicationService.updatePassword(passwordUpdateCommand);
  }

  /**
   * 指定したIDのユーザーを削除する。
   *
   * @param userId 削除対象のユーザーID
   */
  @Operation(summary = "ユーザー削除", description = "指定したIDのユーザーを削除します")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "削除成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUserById(@PathVariable UUID userId) {
    userApplicationService.deleteUserById(userId);
  }
}
