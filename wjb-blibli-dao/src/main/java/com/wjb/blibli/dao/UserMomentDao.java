package com.wjb.blibli.dao;

import com.wjb.blibli.domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMomentDao {
    Integer addUserMoment(UserMoment userMoment);
}
