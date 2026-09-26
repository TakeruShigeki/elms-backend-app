package com.everrefine.elms.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
/** ローカル環境のシードデータ初期化。 */
public class LocalSeedInitializer implements ApplicationRunner {

  @Value("${upload.directory}")
  private String uploadDirectory;

  private final ResourcePatternResolver resourcePatternResolver;

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Path uploadPath = Paths.get(uploadDirectory);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    Resource[] resources = resourcePatternResolver.getResources("classpath:seed-images/*");
    for (Resource resource : resources) {
      Path dest = uploadPath.resolve(resource.getFilename());
      Files.copy(resource.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
      log.info("シード画像をローカルストレージにコピーしました: {}", resource.getFilename());
    }
  }
}
