package com.everrefine.elms.application.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/** ファイルストレージアプリケーションサービスのインターフェース。 */
public interface FileStorageApplicationService {

  /**
   * 画像ファイルを保存する。
   *
   * @param file 保存する画像ファイル
   * @return 保存先URL
   * @throws IOException ファイル操作に失敗した場合
   */
  String saveImage(MultipartFile file) throws IOException;

  /**
   * 未使用ファイルを削除する。
   *
   * @throws IOException ファイル操作に失敗した場合
   */
  void delete() throws IOException;
}
