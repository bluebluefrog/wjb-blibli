package com.wjb.blibli.controller;

import com.wjb.blibli.controller.support.UserSupport;
import com.wjb.blibli.domain.JsonResponse;
import com.wjb.blibli.domain.auth.UserAuthorities;
import com.wjb.blibli.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthController {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities(){
        Long currentUserId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(currentUserId);
        return new JsonResponse<>(userAuthorities);
    }
}
