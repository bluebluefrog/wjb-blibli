package com.wjb.blibli.service;

public interface UserCoinService {

    Integer getUserCoinsAmount(Long userId);

    void updateUserCoinsAmount(Long userId, Integer amount);
}
