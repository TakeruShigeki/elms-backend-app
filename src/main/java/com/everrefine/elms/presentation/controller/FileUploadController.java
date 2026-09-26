package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.service.FileStorageApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** ファイルアップロードに関するコントローラー。 */
@Tag(name = "ファイルアップロード")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

  private final FileStorageApplicationService fileStorageApplicationService;

  /**
   * 画像ファイルをアップロードし、保存先パスを返す。
   *
   * @param file アップロード対象の画像ファイル
   * @return 保存先パス
   */
  @Operation(summary = "ファイルアップロード", description = "画像ファイルをアップロードし、保存先パスを返します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "アップロード成功（レスポンスボディにファイルパスを返す）"),
    @ApiResponse(responseCode = "400", description = "ファイルが未指定または空です"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "500", description = "ファイルの保存に失敗しました")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/upload")
  public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file)
      throws IOException {
    // 保存失敗などの想定外の例外はGlobalExceptionHandlerに委ね、500として扱う。
    // ここで握りつぶすと、サーバー側の異常がクライアント起因の400として返ってしまう。
    String filePath = fileStorageApplicationService.saveImage(file);
    return ResponseEntity.ok(filePath); // 保存先のパスを返す
  }
}
