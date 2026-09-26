package com.everrefine.elms.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@Profile("prd")
/** S3設定。 */
public class S3Config {

  @Value("${cloud.aws.s3.endpoint:}")
  private String endpoint;

  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  @Value("${cloud.aws.region.static:ap-northeast-1}")
  private String region;

  /**
   * S3クライアントを生成する。
   *
   * @return S3クライアント
   */
  @Bean
  public S3Client s3Client() {
    S3ClientBuilder builder =
        S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));

    if (!endpoint.isEmpty()) {
      // LocalStack / MinIO はパス形式URLが必要
      builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
    }

    return builder.build();
  }
}
