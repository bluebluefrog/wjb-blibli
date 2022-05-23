package com.wjb.blibli.service;

import com.wjb.blibli.domain.FollowingGroup;
import com.wjb.blibli.domain.UserFollowing;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserFollowingService {

    @Transactional
    void addUserFollowings(UserFollowing userFollowing);

    //1获取关注的用户列表
    //2根据关注的用户id查询关注用户的基本信息
    //3将关注用户按关注分组进行分类
    List<FollowingGroup> getUserFollowings(Long userId);

    //1获取当前用户粉丝列表
    //2根据粉丝的用户id查询基本信息
    //3查询当前用户是否已经关注该粉丝
    List<UserFollowing> getUserFans(Long userId);
}
