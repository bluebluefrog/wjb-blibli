package com.wjb.blibli.service;

import com.wjb.blibli.domain.auth.UserRole;

import java.util.List;

public interface UserRoleService {
    List<UserRole> getUserRoleByUserId(Long userId);

    void addUserRole(UserRole userRole);
}
