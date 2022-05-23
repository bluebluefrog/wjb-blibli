package com.wjb.blibli.controller;

import com.wjb.blibli.controller.support.UserSupport;
import com.wjb.blibli.domain.JsonResponse;
import com.wjb.blibli.domain.UserMoment;
import com.wjb.blibli.domain.annotation.ApiLimitedRole;
import com.wjb.blibli.domain.annotation.DataLimited;
import com.wjb.blibli.domain.auth.AuthRole;
import com.wjb.blibli.domain.constant.AuthRoleConstant;
import com.wjb.blibli.service.UserMomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserMomentController {

    @Autowired
    private UserMomentService userMomentService;

    @Autowired
    private UserSupport userSupport;

    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0})
    @DataLimited
    @PostMapping("/user-moments")
    public JsonResponse<String> adUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long currentUserId = userSupport.getCurrentUserId();
        userMoment.setUserId(currentUserId);
        userMomentService.addUserMoment(userMoment);
        return JsonResponse.success();
    }

    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments() {
        Long currentUserId = userSupport.getCurrentUserId();
        List<UserMoment> resultList=userMomentService.getUserSubscribedMoments(currentUserId);
        return new JsonResponse<>(resultList);
    }
}
