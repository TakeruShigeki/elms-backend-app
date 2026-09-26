package com.everrefine.elms.domain.model.user;

import com.everrefine.elms.domain.model.PagerForRequest;
import jakarta.annotation.Nullable;
import java.time.LocalDate;

/** ユーザー検索条件の値オブジェクト。 */
public record UserSearchCondition(
    PagerForRequest pagerForRequest,
    @Nullable String userId,
    @Nullable String userRole,
    @Nullable String realName,
    @Nullable String userName,
    @Nullable String emailAddress,
    @Nullable LocalDate createdDateFrom,
    @Nullable LocalDate createdDateTo) {

  /**
   * ユーザー検索条件を作成する。
   *
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @param userId ユーザーID（nullの場合は絞り込みなし）
   * @param userRole 権限（nullの場合は絞り込みなし）
   * @param realName 氏名（nullの場合は絞り込みなし）
   * @param userName ユーザー名（nullの場合は絞り込みなし）
   * @param emailAddress メールアドレス（nullの場合は絞り込みなし）
   * @param createdDateFrom 作成日From（nullの場合は絞り込みなし）
   * @param createdDateTo 作成日To（nullの場合は絞り込みなし）
   */
  public UserSearchCondition(
      int pageNum,
      int pageSize,
      String userId,
      String userRole,
      String realName,
      String userName,
      String emailAddress,
      LocalDate createdDateFrom,
      LocalDate createdDateTo) {
    this(
        new PagerForRequest(pageNum, pageSize),
        userId,
        userRole,
        realName,
        userName,
        emailAddress,
        createdDateFrom,
        createdDateTo);
  }

  /**
   * 1ページ当たりの件数を返す。
   *
   * @return 1ページ当たりの件数
   */
  public int getPageSize() {
    return pagerForRequest.pageSize();
  }

  /**
   * DBクエリ用のオフセット値を返す。
   *
   * @return オフセット値
   */
  public int getOffset() {
    return pagerForRequest.getOffset();
  }
}
