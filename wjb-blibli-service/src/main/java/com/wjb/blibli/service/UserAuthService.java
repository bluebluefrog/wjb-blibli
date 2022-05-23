package com.wjb.blibli.service;

import com.wjb.blibli.domain.auth.UserAuthorities;

public interface UserAuthService {
    UserAuthorities getUserAuthorities(Long currentUserId);

    void addUserDefaultRole(Long id);
}
