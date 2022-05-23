package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.AuthRoleElementOperationDao;
import com.wjb.blibli.domain.auth.AuthRoleElementOperation;
import com.wjb.blibli.service.AuthRoleElementOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleElementOperationServiceImpl implements AuthRoleElementOperationService {

    @Autowired
    private AuthRoleElementOperationDao authRoleElementOperationDao;

    public List<AuthRoleElementOperation> getRoleElementOperationByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationDao.getRoleElementOperationByRoleIds(roleIdSet);

    }
}
