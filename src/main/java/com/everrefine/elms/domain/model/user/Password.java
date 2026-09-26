package com.everrefine.elms.domain.model.user;

import com.everrefine.elms.domain.exception.InvalidValueException;
import java.util.regex.Pattern;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** パスワードの値オブジェクト。 */
public record Password(String value) {

  // BCrypt形式であることを確認する正規表現
  private static final Pattern BCRYPT_PATTERN =
      Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

  /**
   * BCrypt形式のパスワード文字列からパスワードを作成する。
   *
   * @param value BCrypt形式のパスワード文字列
   */
  public Password {
    if (value == null || !BCRYPT_PATTERN.matcher(value).matches()) {
      throw new InvalidValueException("パスワードの形式が不正です");
    }
  }

  /**
   * 平文パスワードをBCryptでハッシュ化してパスワードを作成する。
   *
   * @param value 平文パスワード
   * @return ハッシュ化されたパスワード
   */
  public static Password encryptAndCreate(String value) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    return new Password(encoder.encode(value));
  }

  /**
   * 平文パスワードとハッシュ化済みパスワードが一致するかを判定する。
   *
   * @param rawPassword 平文パスワード
   * @return 一致する場合はtrue
   */
  public boolean matches(String rawPassword) {
    return BCrypt.checkpw(rawPassword, this.value);
  }
}
