package com.everrefine.elms.presentation.scheduler;

import com.everrefine.elms.application.service.FileStorageApplicationService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** 未使用ファイル削除スケジューラー。 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DeleteUnusedFilesScheduler {

  private final FileStorageApplicationService fileStorageApplicationService;

  /**
   * 未使用ファイルを削除する。
   *
   * @throws IOException ファイル操作に失敗した場合
   */
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tokyo")
  public void deleteUnusedFiles() throws IOException {
    fileStorageApplicationService.delete();
  }
}
