package com.everrefine.elms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("dev")
/** ローカルストレージ設定。 */
public class LocalStorageConfig implements WebMvcConfigurer {

  @Value("${upload.directory}")
  private String uploadDirectory;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDirectory + "/");
  }
}
