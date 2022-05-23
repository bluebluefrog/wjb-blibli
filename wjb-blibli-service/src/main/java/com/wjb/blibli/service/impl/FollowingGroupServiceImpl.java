package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.FollowingGroupDao;
import com.wjb.blibli.domain.FollowingGroup;
import com.wjb.blibli.service.FollowingGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowingGroupServiceImpl implements FollowingGroupService {


    @Autowired
    private FollowingGroupDao followingGroupDao;


    public FollowingGroup getByType(String type){
        return followingGroupDao.getByType(type);
    }

    public FollowingGroup getById(Long id){
        return followingGroupDao.getById(id);
    }

    public List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupDao.getByUserId(userId);
    }

}
