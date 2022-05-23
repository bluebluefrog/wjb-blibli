package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.AuthRoleMenuDao;
import com.wjb.blibli.domain.auth.AuthRoleMenu;
import com.wjb.blibli.service.AuthRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleMenuServiceImpl implements AuthRoleMenuService {

    @Autowired
    private AuthRoleMenuDao authRoleMenuDao;


    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuDao.getAuthRoleMenusByRoleIds(roleIdSet);
    }
}
