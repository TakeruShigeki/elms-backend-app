package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.LoginHistoryCreateCommand;
import com.everrefine.elms.application.command.PasswordUpdateCommand;
import com.everrefine.elms.application.command.UserCreateCommand;
import com.everrefine.elms.application.command.UserImportCommand;
import com.everrefine.elms.application.command.UserSearchCommand;
import com.everrefine.elms.application.command.UserUpdateCommand;
import com.everrefine.elms.application.dto.AuthenticatedUserDto;
import com.everrefine.elms.application.dto.UserDto;
import com.everrefine.elms.application.dto.UserImportResponseDto;
import com.everrefine.elms.application.dto.UserPageDto;
import java.util.UUID;
import org.springframework.core.io.Resource;

/** ユーザーアプリケーションサービスのインターフェース。 */
public interface UserApplicationService {

  /**
   * IDでユーザーを取得する。
   *
   * @param userId ユーザーID
   * @return ユーザーDTO
   */
  UserDto findUserById(UUID userId);

  /**
   * 認証用にユーザーをIDで取得する。
   *
   * @param userId ユーザーID
   * @return 認証用ユーザーDTO
   */
  AuthenticatedUserDto findAuthenticatedUserById(UUID userId);

  /**
   * 認証用にユーザーをメールアドレスで取得する。
   *
   * @param emailAddress メールアドレス
   * @return 認証用ユーザーDTO
   */
  AuthenticatedUserDto findAuthenticatedUserByEmail(String emailAddress);

  /**
   * ユーザー一覧をページング取得する。
   *
   * @param userSearchCommand ユーザー検索Command
   * @return ユーザーページDTO
   */
  UserPageDto findUsers(UserSearchCommand userSearchCommand);

  /**
   * ユーザーを作成する。
   *
   * @param userCreateCommand ユーザー作成Command
   */
  void createUser(UserCreateCommand userCreateCommand);

  /**
   * ユーザーを更新する。
   *
   * @param userUpdateCommand ユーザー更新Command
   */
  void updateUser(UserUpdateCommand userUpdateCommand);

  /**
   * IDでユーザーを削除する。
   *
   * @param userId ユーザーID
   */
  void deleteUserById(UUID userId);

  /**
   * ユーザーのログイン履歴を更新する。
   *
   * @param loginHistoryCreateCommand ログイン履歴作成Command
   */
  void updateUserLoginHistory(LoginHistoryCreateCommand loginHistoryCreateCommand);

  /**
   * ユーザー一覧をCSV形式でエクスポートする。
   *
   * @return CSVリソース
   */
  Resource exportUsersCsv();

  /**
   * パスワードを更新する。
   *
   * @param passwordUpdateCommand パスワード更新Command
   */
  void updatePassword(PasswordUpdateCommand passwordUpdateCommand);

  /**
   * CSVファイルをアップロードしてユーザー情報を一括更新する。
   *
   * @param userImportCommand ユーザー取込用Command
   * @return 取込した件数
   */
  UserImportResponseDto importUsersCsv(UserImportCommand userImportCommand);
}
