package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.AuthRoleDao;
import com.wjb.blibli.domain.auth.AuthRole;
import com.wjb.blibli.domain.auth.AuthRoleElementOperation;
import com.wjb.blibli.domain.auth.AuthRoleMenu;
import com.wjb.blibli.service.AuthRoleElementOperationService;
import com.wjb.blibli.service.AuthRoleMenuService;
import com.wjb.blibli.service.AuthRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleServiceImpl implements AuthRoleService {

    @Autowired
    private AuthRoleElementOperationService authRoleElementOperationService;

    @Autowired
    private AuthRoleMenuService authRoleMenuService;

    @Autowired
    private AuthRoleDao authRoleDao;

    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationByRoleIds(roleIdSet);
    }

    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuService.getAuthRoleMenusByRoleIds(roleIdSet);
    }

    public AuthRole getRoleByCode(String code) {
        return authRoleDao.getRoleByCode(code);
    }

}
