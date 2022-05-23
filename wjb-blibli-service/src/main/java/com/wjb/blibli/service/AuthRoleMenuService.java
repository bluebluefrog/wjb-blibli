package com.wjb.blibli.service;

import com.wjb.blibli.domain.auth.AuthRoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface AuthRoleMenuService {
    List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet);
}
