package com.everrefine.elms.domain.model.user;

/** ユーザーロールを表す列挙型。 */
public enum UserRole {
  GENERAL("一般"),
  ADMIN("管理者");

  private final String roleName;

  /**
   * ユーザーロールのコンストラクタ。
   *
   * @param roleName ロールの表示名
   */
  UserRole(String roleName) {
    this.roleName = roleName;
  }

  /**
   * ロールの表示名を返す。
   *
   * @return ロールの表示名
   */
  public String getRoleName() {
    return roleName;
  }

  /**
   * Spring Securityで使用するロールコードを返す。
   *
   * @return ロールコード（enum名）
   */
  public String getCode() {
    return name();
  }

  /**
   * 表示名からユーザーロールに変換する。
   *
   * @param roleName 表示名
   * @return ユーザーロール
   */
  public static UserRole fromRoleName(String roleName) {
    for (UserRole userRole : values()) {
      if (userRole.getRoleName().equals(roleName)) {
        return userRole;
      }
    }
    throw new IllegalArgumentException("権限が不正です");
  }
}
