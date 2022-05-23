package com.wjb.blibli.dao;

import com.wjb.blibli.domain.UserFollowing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserFollowingDao {
    void deleteUserFollowing(@Param("userId") Long userId, @Param("followingId")Long followingId);

    void addUserFollowing(UserFollowing userFollowing);

    List<UserFollowing> getFollowingsByUserId(Long userId);

    List<UserFollowing> getUserFans(Long userId);
}
