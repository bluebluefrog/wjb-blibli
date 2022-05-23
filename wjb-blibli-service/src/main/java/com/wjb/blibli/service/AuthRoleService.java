package com.wjb.blibli.service;

import com.wjb.blibli.domain.auth.AuthRole;
import com.wjb.blibli.domain.auth.AuthRoleElementOperation;
import com.wjb.blibli.domain.auth.AuthRoleMenu;

import java.util.List;
import java.util.Set;

public interface AuthRoleService {
    List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet);

    List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet);

    AuthRole getRoleByCode(String roleLv0);
}
