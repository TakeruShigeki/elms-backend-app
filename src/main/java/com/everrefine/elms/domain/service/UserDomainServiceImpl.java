package com.everrefine.elms.domain.service;

import org.springframework.stereotype.Component;

/** {@link UserDomainService} の実装。 */
@Component
public class UserDomainServiceImpl implements UserDomainService {

  @Override
  public boolean matchesPassword(String password, String confirmPassword) {
    return password != null && password.equals(confirmPassword);
  }
}
