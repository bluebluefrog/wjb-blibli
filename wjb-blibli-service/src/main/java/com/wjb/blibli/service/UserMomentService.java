package com.wjb.blibli.service;

import com.wjb.blibli.domain.UserMoment;

import java.util.List;

public interface UserMomentService {
    public void addUserMoment(UserMoment userMoment) throws Exception;

    List<UserMoment> getUserSubscribedMoments(Long currentUserId);
}
