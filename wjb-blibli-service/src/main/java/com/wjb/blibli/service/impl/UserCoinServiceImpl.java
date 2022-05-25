package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.UserCoinDao;
import com.wjb.blibli.service.UserCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCoinServiceImpl implements UserCoinService {

    @Autowired
    private UserCoinDao userCoinDao;

    @Override
    public Integer getUserCoinsAmount(Long userId) {
        return null;
    }

    @Override
    public void updateUserCoinsAmount(Long userId, Integer amount) {

    }
}
