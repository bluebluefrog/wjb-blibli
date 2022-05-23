package com.wjb.blibli.dao;

import com.wjb.blibli.domain.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserRoleDao {

    List<UserRole> getUserRoleByUserId(Long userId);

    Integer addUserRole(UserRole userRole);
}
