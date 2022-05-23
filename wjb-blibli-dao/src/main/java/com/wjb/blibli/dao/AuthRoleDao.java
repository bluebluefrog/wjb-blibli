package com.wjb.blibli.dao;

import com.wjb.blibli.domain.auth.AuthRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AuthRoleDao {
    AuthRole getRoleByCode(String code);
}
