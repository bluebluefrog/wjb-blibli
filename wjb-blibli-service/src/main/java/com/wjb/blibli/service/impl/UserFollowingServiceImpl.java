package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.UserFollowingDao;
import com.wjb.blibli.domain.FollowingGroup;
import com.wjb.blibli.domain.User;
import com.wjb.blibli.domain.UserFollowing;
import com.wjb.blibli.domain.UserInfo;
import com.wjb.blibli.domain.constant.UserConstant;
import com.wjb.blibli.domain.exception.ConditionException;
import com.wjb.blibli.service.FollowingGroupService;
import com.wjb.blibli.service.UserFollowingService;
import com.wjb.blibli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingServiceImpl implements UserFollowingService {

    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if (groupId == null) {
            //如果分组id不存在寻找默认分组id
            FollowingGroup group = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(group.getId());
        } else {
            //分组id存在则查询id是否合法
            FollowingGroup group = followingGroupService.getById(userFollowing.getGroupId());
            if (group == null) {
                throw new ConditionException("关注分组不存在!");
            }
        }
        //验证关注的用户是否存在
        User user = userService.getById(userFollowing.getFollowingId());
        if (user == null) {
            throw new ConditionException("关注用户不存在!");
        }
        //若要添加先删除已存在的记录
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), userFollowing.getFollowingId());
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }


    //1获取关注的用户列表
    //2根据关注的用户id查询关注用户的基本信息
    //3将关注用户按关注分组进行分类
    public List<FollowingGroup> getUserFollowings(Long userId) {
        List<FollowingGroup> result = new ArrayList<>();

        //查询用户关注的所有关注用户id
        List<UserFollowing> userFollowings = userFollowingDao.getFollowingsByUserId(userId);
        Set<Long> followerIds = userFollowings.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        List<UserInfo> followerInfo = new ArrayList<>();
        if (followerIds.size() > 0) {
            //通过所有关注的用户id批量查询该关注用户info
            followerInfo = userService.getUserInfoByIds(followerIds);

        }
        for (UserFollowing userFollowing : userFollowings) {
            for (UserInfo userInfo : followerInfo) {
                //把结果进行比较如果相同则set进userFollowing
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        //搜索出所有关注分类
        List<FollowingGroup> followingGroupList = followingGroupService.getByUserId(userId);
        //处理全部关注分类
        //创建一个全部关注分类
        FollowingGroup allGroup = new FollowingGroup();
        //增加全部关注分类名字
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        //将所有该用户关注的用户添加进全部分类
        allGroup.setFollowingUserInfoList(followerInfo);
        //结果中添加全部分类
        result.add(allGroup);
        //处理个人个性化分类
        //循环用户全部分类给每个分类都添加该分类下拥有的关注用户
        for (FollowingGroup followingGroup : followingGroupList) {
            //创建一个用来装该分类下info的list待添加
            List<UserInfo> userInfosByGroup = new ArrayList<>();
            //循环用户关注
            for (UserFollowing userFollowing : userFollowings) {
                //如果发现分类的id和用户关注id相同，那么这个这个关注就属于目前分类
                if (followingGroup.getId().equals(userFollowing.getGroupId())) {
                    userInfosByGroup.add(userFollowing.getUserInfo());
                }
            }
            //给分类添加infoList
            followingGroup.setFollowingUserInfoList(userInfosByGroup);
            //将分类添加进结果
            result.add(followingGroup);
        }
        return result;
    }

    //1获取当前用户粉丝列表
    //2根据粉丝的用户id查询基本信息
    //3查询当前用户是否已经关注该粉丝
    public List<UserFollowing> getUserFans(Long userId) {
        //获取粉丝列表
        List<UserFollowing> fans = userFollowingDao.getUserFans(userId);
        Set<Long> fanIds = fans.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());
        //粉丝的信息
        List<UserInfo> userInfoByIds = new ArrayList<>();
        if (fanIds.size() > 0) {
            userInfoByIds = userService.getUserInfoByIds(fanIds);
        }
        //查看是否有登录用户已经关注的fan
        List<UserFollowing> followingsByUserList = userFollowingDao.getFollowingsByUserId(userId);
        for (UserFollowing fan : fans) {
            //给粉丝列表进行赋值userInfo操作
            for (UserInfo userInfo : userInfoByIds) {
                if (fan.getUserId().equals(userInfo.getUserId())) {
                    //互关暂时设置成false
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            //查看是否互关并设置
            for (UserFollowing following : followingsByUserList) {
                if (following.getFollowingId().equals(fan.getUserId())) {
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fans;
    }

}
