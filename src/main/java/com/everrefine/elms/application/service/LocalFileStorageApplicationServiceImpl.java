package com.everrefine.elms.application.service;

import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.domain.repository.CourseRepository;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** ローカルファイルストレージアプリケーションサービスの実装。 */
@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageApplicationServiceImpl implements FileStorageApplicationService {

  private static final String URL_PREFIX = "/uploads/";

  @Value("${upload.directory}")
  private String uploadDirectory;

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final LessonRepository lessonRepository;

  @Override
  public String saveImage(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("ファイルを指定してください");
    }

    Path uploadPath = Paths.get(uploadDirectory);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
    Files.copy(file.getInputStream(), uploadPath.resolve(filename));
    return URL_PREFIX + filename;
  }

  @Transactional(readOnly = true)
  @Override
  public void delete() throws IOException {
    log.info("未使用ファイルの削除バッチを実行します（ローカルストレージ）。");

    Path uploadPath = Paths.get(uploadDirectory);
    if (!Files.exists(uploadPath)) {
      log.info("アップロードディレクトリが存在しません。");
      return;
    }

    List<String> dbKeys;
    try {
      dbKeys = getKeysFromDb();
    } catch (DataAccessException e) {
      log.error("DBの接続に失敗しました。");
      return;
    }

    Set<String> dbKeySet = new HashSet<>(dbKeys);
    List<Path> orphanFiles;
    try (var stream = Files.list(uploadPath)) {
      orphanFiles = stream.filter(p -> !dbKeySet.contains(p.getFileName().toString())).toList();
    }

    if (orphanFiles.isEmpty()) {
      log.info("削除対象のファイルは見つかりませんでした。");
      return;
    }

    for (Path file : orphanFiles) {
      try {
        Files.delete(file);
        log.info("未使用ファイルを削除しました: {}", file.getFileName());
      } catch (IOException e) {
        log.warn("ファイルの削除に失敗しました: {}", file.getFileName());
      }
    }
    log.info("未使用ファイルの削除バッチの実行が完了しました。");
  }

  /**
   * DBに登録されているファイルキーを取得する。
   *
   * @return DBに登録されているファイルキーのリスト
   */
  private List<String> getKeysFromDb() {
    List<String> urls = new java.util.ArrayList<>();
    urls.addAll(userRepository.findByThumbnailUrlStartingWith(URL_PREFIX));
    urls.addAll(courseRepository.findByThumbnailUrlStartingWith(URL_PREFIX));
    urls.addAll(lessonRepository.findByVideoUrlStartingWith(URL_PREFIX));
    return urls.stream().map(url -> url.substring(URL_PREFIX.length())).toList();
  }

  /**
   * ファイル名から拡張子を取得する。
   *
   * @param filename ファイル名
   * @return 拡張子（ドット込み）、拡張子がない場合は空文字
   */
  private String getExtension(String filename) {
    if (filename != null && filename.contains(".")) {
      return filename.substring(filename.lastIndexOf("."));
    }
    return "";
  }
}
