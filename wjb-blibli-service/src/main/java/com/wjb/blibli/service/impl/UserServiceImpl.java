package com.wjb.blibli.service.impl;

import com.mysql.cj.util.StringUtils;
import com.wjb.blibli.dao.UserDao;
import com.wjb.blibli.domain.RefreshTokenDetail;
import com.wjb.blibli.domain.User;
import com.wjb.blibli.domain.UserInfo;
import com.wjb.blibli.domain.constant.UserConstant;
import com.wjb.blibli.domain.exception.ConditionException;
import com.wjb.blibli.service.UserAuthService;
import com.wjb.blibli.service.UserService;
import com.wjb.blibli.util.MD5Util;
import com.wjb.blibli.util.RSAUtil;
import com.wjb.blibli.util.TokenUtil;
import jdk.nashorn.internal.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthService userAuthService;

    @Transactional
    public void add(User user) {
        String phone = user.getPhone();
        //判断手机号是否合法
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        //判断手机号是否被注册
        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("该手机号已被注册!");
        }
        //MD5加密用户密码
        Date currentTime = new Date();
        //使用时间戳生成盐值
        String salt = String.valueOf(currentTime.getTime());
        String password = user.getPassword();
        String originalPassword;

        try {
            originalPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String md5Password=MD5Util.sign(originalPassword, salt, "UTF-8");
        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(currentTime);
        userDao.addUser(user);
        //生成了用户所以添加用户信息
        UserInfo userinfo = new UserInfo();
        //添加用户后我们进行了回填充所以可以获取到id
        userinfo.setUserId(user.getId());
        userinfo.setNick(UserConstant.DEFAULT_NICK);
        userinfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userinfo.setGender(UserConstant.GENDER_UNKNOW);
        userinfo.setCreateTime(currentTime);
        userDao.addUserInfo(userinfo);
        //添加用户默认权限
        userAuthService.addUserDefaultRole(user.getId());
    }

    public String login(User user) throws Exception{
        String phone = user.getPhone();
        //判断手机号是否为空
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser==null) {
            throw new ConditionException("用户不存在!");
        }
        //解密从前端传来的密码然后加密成md5进行比较
        String password = user.getPassword();

        String originalPassword;

        try {
            originalPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(originalPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("密码错误！");
        }
        //生成用户令牌
        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        User user=userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public User getUserByPhone(String phone){
      return userDao.getUserByPhone(phone);
  }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public void updateUser(User user) {
        user.setUpdateTime(new Date());
        userDao.updateUser(user);
    }

    public User getById(Long userId) {
        return userDao.getUserById(userId);
    }

    public List<UserInfo> getUserInfoByIds(Set<Long> followingIds) {
        return userDao.getUserInfoByUserIds(followingIds);
    }

    //双令牌登录
    public Map<String, Object> loginForDts(User user)throws Exception{
        String phone = user.getPhone();
        //判断手机号是否为空
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser==null) {
            throw new ConditionException("用户不存在!");
        }
        //解密从前端传来的密码然后加密成md5进行比较
        String password = user.getPassword();

        String originalPassword;

        try {
            originalPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(originalPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("密码错误！");
        }
        //生成用户令牌
        Long userId = dbUser.getId();
        String accessToken= TokenUtil.generateToken(userId);
        //从这里开始与之前的登陆不同
        String refreshToken = TokenUtil.generateRefreshToken(userId);
        //保存refresh token到数据库,使用先删除再增加的方法不考虑更新
        //我们将刷新token和userId存到数据库是为了在用户访问时我们会检查数据库里的token查看是否到期要重新登陆
        userDao.deleteRefreshToken(refreshToken, userId);
        userDao.addRefreshToken(refreshToken, userId,new Date());
        Map<String, Object> result = new HashMap<>();
        //返回双token
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    //登出时需要将数据库中的refreshToken删除
    public void logout(String refreshToken, Long userId) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    //当Token过期,将token重新生成
    public String refreshAccessToken(String refreshToken) throws Exception {
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken);
        if (refreshTokenDetail == null) {
            //这时说明refreshToken也过期了,就不能续时登陆了
            throw new ConditionException("555", "token expire!");
        }
        Long userId = refreshTokenDetail.getUserId();
        return TokenUtil.generateToken(userId);
    }
}
