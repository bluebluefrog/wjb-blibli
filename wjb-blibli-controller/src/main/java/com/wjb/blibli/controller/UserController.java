package com.wjb.blibli.controller;

import com.wjb.blibli.controller.support.UserSupport;
import com.wjb.blibli.domain.JsonResponse;
import com.wjb.blibli.domain.User;
import com.wjb.blibli.domain.UserInfo;
import com.wjb.blibli.service.UserService;
import com.wjb.blibli.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSupport userSupport;


    //用于获取加密公钥
    @GetMapping("/ras-pks")
    public JsonResponse<String> getRasPublicKey(){
        String pk = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(pk);
    }

    //注册用户
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user) {
        userService.add(user);
        return JsonResponse.success();
    }

    //用户登录
    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user)throws Exception {
        String token=userService.login(user);
        return new JsonResponse<>(token);
    }

    //获取用户信息
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo() {
        Long userId = userSupport.getCurrentUserId();
        User user=userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    //条件更新用户
    @PutMapping("/user")
    public JsonResponse<String> updateUser(@RequestBody User user) {
        //为了防止伪造userId我们都从token中获取userId
        Long userId = userSupport.getCurrentUserId();
        userService.updateUser(user);
        return JsonResponse.success();
    }

    //条件更新用户信息
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo) {
        //为了防止伪造userId我们都从token中获取userId
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }

    //用双令牌实现登录
    @PostMapping("/user-dts")
    public JsonResponse<Map<String, Object>> loginForDts(@RequestBody User user) throws Exception {
        Map<String, Object> map = userService.loginForDts(user);
        return new JsonResponse<>(map);
    }

    //登出
    @DeleteMapping("/refresh-tokens")
    public JsonResponse<String> logout(HttpServletRequest request){
        //登出需要删除refreshTokens我们从request中获取
        String refreshToken = request.getHeader("refreshToken");
        Long userId = userSupport.getCurrentUserId();
        userService.logout(refreshToken, userId);
        return JsonResponse.success();
    }

    //刷新accessToken
    @PostMapping("/access-tokens")
    public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception {
        String refreshToken = request.getHeader("refreshToken");
        String accessToken = userService.refreshAccessToken(refreshToken);
        return new JsonResponse<>(accessToken);
    }
}
