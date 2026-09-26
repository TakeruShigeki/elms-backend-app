package com.everrefine.elms.presentation.security;

import com.everrefine.elms.application.dto.AuthenticatedUserDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.application.service.UserApplicationService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Spring Security用のユーザー詳細サービス。 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserApplicationService userApplicationService;

  /**
   * CustomUserDetailsServiceのコンストラクタ。
   *
   * @param userApplicationService ユーザーアプリケーションサービス
   */
  public CustomUserDetailsService(UserApplicationService userApplicationService) {
    this.userApplicationService = userApplicationService;
  }

  @Override
  public UserDetails loadUserByUsername(String emailAddress) {
    try {
      return toUserDetails(userApplicationService.findAuthenticatedUserByEmail(emailAddress));
    } catch (ResourceNotFoundException e) {
      throw new UsernameNotFoundException("User not found");
    }
  }

  /**
   * ユーザーIDでユーザー詳細を取得する。
   *
   * @param userId ユーザーID
   * @return ユーザー詳細
   * @throws UsernameNotFoundException ユーザーが存在しない場合
   */
  public UserDetails loadUserById(String userId) {
    try {
      return toUserDetails(
          userApplicationService.findAuthenticatedUserById(UUID.fromString(userId)));
    } catch (ResourceNotFoundException e) {
      throw new UsernameNotFoundException("User not found");
    } catch (UsernameNotFoundException e) {
      throw e;
    } catch (Exception e) {
      // DBデータ不正（パスワードフォーマット破損など）はUsernameNotFoundExceptionに変換し、
      // JwtFilterのtry-catchで適切に処理させる
      throw new UsernameNotFoundException("Failed to load user: " + userId, e);
    }
  }

  private UserDetails toUserDetails(AuthenticatedUserDto user) {
    return new org.springframework.security.core.userdetails.User(
        user.id().toString(),
        user.encodedPassword(),
        List.of(new SimpleGrantedAuthority(user.roleCode())));
  }
}
