package com.everrefine.elms.application.service;

import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.domain.repository.CourseRepository;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** S3ファイルストレージアプリケーションサービスの実装。 */
@Service
@Profile("prd")
@Slf4j
public class S3FileStorageApplicationServiceImpl implements FileStorageApplicationService {

  private final S3Client s3Client;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final LessonRepository lessonRepository;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.public-url}")
  private String publicUrl;

  /**
   * コンストラクタ。
   *
   * @param s3Client S3クライアント
   * @param userRepository ユーザーリポジトリ
   * @param courseRepository コースリポジトリ
   * @param lessonRepository レッスンリポジトリ
   */
  public S3FileStorageApplicationServiceImpl(
      S3Client s3Client,
      UserRepository userRepository,
      CourseRepository courseRepository,
      LessonRepository lessonRepository) {
    this.s3Client = s3Client;
    this.userRepository = userRepository;
    this.courseRepository = courseRepository;
    this.lessonRepository = lessonRepository;
  }

  @Override
  public String saveImage(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("ファイルを指定してください");
    }

    String key = UUID.randomUUID() + getExtension(file.getOriginalFilename());

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build(),
        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

    return publicUrl + "/" + key;
  }

  @Transactional(readOnly = true)
  @Override
  public void delete() throws IOException {
    log.info("未使用ファイルの削除バッチを実行します。");

    List<String> dbKeys;
    try {
      dbKeys = getKeysFromDb();
    } catch (DataAccessException e) {
      log.error("DBの接続に失敗しました。");
      return;
    }

    List<String> s3Keys = listAllS3Keys();
    if (s3Keys.isEmpty()) {
      log.info("S3バケットにファイルがありません。未使用ファイルの削除バッチの実行を正常に終了します。");
      return;
    }

    Set<String> dbKeySet = new HashSet<>(dbKeys);
    List<String> orphanKeys = s3Keys.stream().filter(key -> !dbKeySet.contains(key)).toList();

    if (orphanKeys.isEmpty()) {
      log.info("削除対象のファイルは見つかりませんでした。");
      return;
    }

    List<String> deleted = new ArrayList<>();
    List<String> failed = new ArrayList<>();
    for (String key : orphanKeys) {
      try {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        deleted.add(key);
      } catch (S3Exception e) {
        failed.add(key);
        log.warn("未使用ファイルの削除に失敗しました。key: {}", key);
      }
    }

    if (!deleted.isEmpty()) {
      log.info("{}件の未使用ファイルを削除しました。削除したファイル: {}", deleted.size(), String.join(", ", deleted));
    }
    if (!failed.isEmpty()) {
      log.warn("{}件の削除に失敗しました。失敗したファイル: {}", failed.size(), String.join(", ", failed));
    }
    log.info("未使用ファイルの削除バッチの実行が完了しました。");
  }

  /**
   * S3バケット内のすべてのオブジェクトキーを取得する。
   *
   * @return S3オブジェクトキーのリスト
   */
  private List<String> listAllS3Keys() {
    List<String> keys = new ArrayList<>();
    ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).build();
    ListObjectsV2Response response;
    do {
      response = s3Client.listObjectsV2(request);
      response.contents().forEach(obj -> keys.add(obj.key()));
      if (Boolean.TRUE.equals(response.isTruncated())) {
        request = request.toBuilder().continuationToken(response.nextContinuationToken()).build();
      }
    } while (Boolean.TRUE.equals(response.isTruncated()));
    return keys;
  }

  /**
   * DBに登録されているファイルキーを取得する。
   *
   * @return DBに登録されているファイルキーのリスト
   */
  private List<String> getKeysFromDb() {
    String prefix = publicUrl + "/";
    List<String> urls = new ArrayList<>();
    urls.addAll(userRepository.findByThumbnailUrlStartingWith(prefix));
    urls.addAll(courseRepository.findByThumbnailUrlStartingWith(prefix));
    urls.addAll(lessonRepository.findByVideoUrlStartingWith(prefix));
    return urls.stream().map(url -> url.substring(prefix.length())).toList();
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
