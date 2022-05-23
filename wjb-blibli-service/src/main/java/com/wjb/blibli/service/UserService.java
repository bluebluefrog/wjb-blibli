package com.wjb.blibli.service;

import com.wjb.blibli.domain.User;
import com.wjb.blibli.domain.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    void add(User user);

    String login(User user) throws Exception;

    User getUserInfo(Long userId);

    void updateUserInfos(UserInfo userInfo);

    void updateUser(User user);

    User getById(Long userId);

    List<UserInfo> getUserInfoByIds(Set<Long> followingIds);

    Map<String, Object> loginForDts(User user) throws Exception;

    void logout(String refreshToken, Long userId);

    String refreshAccessToken(String refreshToken) throws Exception;
}
