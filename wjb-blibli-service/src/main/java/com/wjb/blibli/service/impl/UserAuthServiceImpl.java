package com.wjb.blibli.service.impl;

import com.wjb.blibli.domain.auth.*;
import com.wjb.blibli.domain.constant.AuthRoleConstant;
import com.wjb.blibli.service.AuthRoleService;
import com.wjb.blibli.service.UserAuthService;
import com.wjb.blibli.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthRoleService authRoleService;

    public UserAuthorities getUserAuthorities(Long userId) {
        //连接查询出该用户对应的角色，并查出name与code
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //用lambda表达式获取到全部的角色id
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        //还要查询操作权限和访问权限
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
        List<AuthRoleMenu> roleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(roleMenuList);
        return userAuthorities;
    }

    public void addUserDefaultRole(Long id) {
        UserRole userRole = new UserRole();
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setUserId(id);
        userRole.setRoleId(role.getId());
        userRoleService.addUserRole(userRole);
    }
}
