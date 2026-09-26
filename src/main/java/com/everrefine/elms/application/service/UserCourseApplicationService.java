package com.everrefine.elms.application.service;

import com.everrefine.elms.application.dto.UserCourseDto;
import java.util.List;
import java.util.UUID;

/** ユーザーコースアプリケーションサービスのインターフェース。 */
public interface UserCourseApplicationService {

  /**
   * ユーザーIDに紐づくコース一覧を取得する。
   *
   * @param userId ユーザーID
   * @return ユーザーコースDTOリスト
   */
  List<UserCourseDto> findUserCourses(UUID userId);
}
