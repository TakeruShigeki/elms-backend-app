package com.everrefine.elms.config;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@Profile("prd")
@RequiredArgsConstructor
@Slf4j
/** S3シードデータ初期化。 */
public class S3SeedInitializer implements ApplicationRunner {

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  private final S3Client s3Client;
  private final ResourcePatternResolver resourcePatternResolver;

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Resource[] resources = resourcePatternResolver.getResources("classpath:seed-images/*");
    for (Resource resource : resources) {
      String key = resource.getFilename();
      try {
        byte[] bytes = resource.getInputStream().readAllBytes();
        s3Client.putObject(
            PutObjectRequest.builder().bucket(bucket).key(key).contentType("image/png").build(),
            RequestBody.fromBytes(bytes));
        log.info("シード画像をS3にアップロードしました: {}", key);
      } catch (Exception e) {
        log.warn("シード画像のアップロードに失敗しました: {} - {}", key, e.getMessage());
      }
    }
  }
}
