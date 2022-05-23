package com.wjb.blibli.dao;

import com.wjb.blibli.domain.auth.AuthRoleElementOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Mapper
@Repository
public interface AuthRoleElementOperationDao {
    List<AuthRoleElementOperation> getRoleElementOperationByRoleIds(@Param("roleIdSet") Set<Long> roleIdSet);
}
