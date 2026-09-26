package com.everrefine.elms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ELMSアプリケーションのエントリーポイント。 */
@SpringBootApplication
public class ElmsApplication {

  /**
   * アプリケーションを起動する。
   *
   * @param args コマンドライン引数
   */
  public static void main(String[] args) {
    SpringApplication.run(ElmsApplication.class, args);
  }
}
