package com.wjb.blibli.service;

import com.wjb.blibli.domain.FollowingGroup;

import java.util.List;

public interface FollowingGroupService {
    FollowingGroup getByType(String type);

    FollowingGroup getById(Long id);

    List<FollowingGroup> getByUserId(Long userId);
}
