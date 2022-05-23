package com.wjb.blibli.dao;

import com.wjb.blibli.domain.FollowingGroup;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface FollowingGroupDao {
    FollowingGroup getById(Long id);

    FollowingGroup getByType(String type);

    List<FollowingGroup> getByUserId(Long userId);
}
