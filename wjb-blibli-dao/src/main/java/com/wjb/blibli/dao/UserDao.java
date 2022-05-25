package com.wjb.blibli.dao;

import com.wjb.blibli.domain.RefreshTokenDetail;
import com.wjb.blibli.domain.User;
import com.wjb.blibli.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Mapper
@Repository
public interface UserDao {

    User getUserByPhone(String phone);

    Integer addUser(User user);

    Integer addUserInfo(UserInfo userInfo);

    User getUserById(Long userId);

    UserInfo getUserInfoByUserId(Long userId);

    void updateUserInfos(UserInfo userInfo);

    void updateUser(User user);

    List<UserInfo> getUserInfoByUserIds(@Param("followingIds") Set<Long> followingIds);

    Integer deleteRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId);

    Integer addRefreshToken(@Param("refreshToken")String refreshToken,  @Param("userId")Long userId, @Param("createTime") Date createTime);

    RefreshTokenDetail getRefreshTokenDetail(@Param("refreshToken") String refreshToken);

    List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList);
