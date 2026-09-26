package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.NewsCreateCommand;
import com.everrefine.elms.application.command.NewsSearchCommand;
import com.everrefine.elms.application.command.NewsUpdateCommand;
import com.everrefine.elms.application.dto.NewsDto;
import com.everrefine.elms.application.dto.NewsPageDto;
import java.util.UUID;

/** お知らせアプリケーションサービスのインターフェース。 */
public interface NewsApplicationService {

  /**
   * IDでお知らせを取得する。
   *
   * @param id お知らせID
   * @return お知らせDTO
   */
  NewsDto findNewsById(UUID id);

  /**
   * お知らせ一覧をページング取得する。
   *
   * @param newsSearchCommands お知らせ検索Command
   * @return お知らせページDTO
   */
  NewsPageDto findNews(NewsSearchCommand newsSearchCommands);

  /**
   * お知らせを作成する。
   *
   * @param newsCreateCommand お知らせ作成Command
   */
  void createNews(NewsCreateCommand newsCreateCommand);

  /**
   * お知らせを更新する。
   *
   * @param newsUpdateCommand お知らせ更新Command
   */
  void updateNews(NewsUpdateCommand newsUpdateCommand);

  /**
   * IDでお知らせを削除する。
   *
   * @param newsId お知らせID
   */
  void deleteNewsById(UUID newsId);
}
