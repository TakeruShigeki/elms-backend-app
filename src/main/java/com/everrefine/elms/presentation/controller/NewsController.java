package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.NewsCreateCommand;
import com.everrefine.elms.application.command.NewsSearchCommand;
import com.everrefine.elms.application.command.NewsUpdateCommand;
import com.everrefine.elms.application.dto.NewsDto;
import com.everrefine.elms.application.dto.NewsPageDto;
import com.everrefine.elms.application.service.NewsApplicationService;
import com.everrefine.elms.presentation.request.NewsCreateRequest;
import com.everrefine.elms.presentation.request.NewsSearchRequest;
import com.everrefine.elms.presentation.request.NewsUpdateRequest;
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

/** お知らせに関するコントローラー。 */
@Tag(name = "お知らせ")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

  private final NewsApplicationService newsApplicationService;

  /**
   * 検索条件・ページ情報を指定してお知らせ一覧を取得する。
   *
   * @param newsSearchRequest お知らせ検索リクエスト（検索条件・ページ情報）
   * @return お知らせページDTO
   */
  @Operation(summary = "お知らせ一覧取得", description = "検索条件・ページ情報を指定してお知らせ一覧を取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません")
  })
  @GetMapping
  public NewsPageDto findNews(@Valid NewsSearchRequest newsSearchRequest) {
    NewsSearchCommand newsSearchCommand = newsSearchRequest.toCommand();
    return newsApplicationService.findNews(newsSearchCommand);
  }

  /**
   * 指定したIDのお知らせを取得する。
   *
   * @param newsId お知らせID
   * @return お知らせDTO
   */
  @Operation(summary = "お知らせ取得", description = "指定したIDのお知らせを取得します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "404", description = "お知らせが見つかりません")
  })
  @GetMapping("/{newsId}")
  public NewsDto findNewsById(@PathVariable UUID newsId) {
    return newsApplicationService.findNewsById(newsId);
  }

  /**
   * 新規お知らせを登録する。
   *
   * @param newsCreateRequest お知らせ作成リクエスト
   */
  @Operation(summary = "お知らせ作成", description = "新規お知らせを登録します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "作成成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping
  public void createNews(@Valid @RequestBody NewsCreateRequest newsCreateRequest) {
    NewsCreateCommand newsCreateCommand = newsCreateRequest.toCommand();
    newsApplicationService.createNews(newsCreateCommand);
  }

  /**
   * 指定したIDのお知らせを更新する。
   *
   * @param newsId 更新対象のお知らせID
   * @param newsUpdateRequest お知らせ更新リクエスト
   */
  @Operation(summary = "お知らせ更新", description = "指定したIDのお知らせを更新します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "404", description = "お知らせが見つかりません")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{newsId}")
  public void updateNews(
      @PathVariable UUID newsId, @Valid @RequestBody NewsUpdateRequest newsUpdateRequest) {
    NewsUpdateCommand newsUpdateCommand = newsUpdateRequest.toCommand(newsId);
    newsApplicationService.updateNews(newsUpdateCommand);
  }

  /**
   * 指定したIDのお知らせを削除する。
   *
   * @param newsId 削除対象のお知らせID
   */
  @Operation(summary = "お知らせ削除", description = "指定したIDのお知らせを削除します")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "削除成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{newsId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteNewsById(@PathVariable UUID newsId) {
    newsApplicationService.deleteNewsById(newsId);
  }
}
