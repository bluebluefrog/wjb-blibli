package com.wjb.blibli.service;

import com.wjb.blibli.domain.auth.AuthRoleElementOperation;

import java.util.List;
import java.util.Set;

public interface AuthRoleElementOperationService {
    List<AuthRoleElementOperation> getRoleElementOperationByRoleIds(Set<Long> roleIdSet);
}
