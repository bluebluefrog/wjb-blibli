package com.wjb.blibli.controller.aspact;

import com.wjb.blibli.controller.support.UserSupport;
import com.wjb.blibli.domain.UserMoment;
import com.wjb.blibli.domain.annotation.ApiLimitedRole;
import com.wjb.blibli.domain.auth.UserRole;
import com.wjb.blibli.domain.constant.AuthRoleConstant;
import com.wjb.blibli.domain.exception.ConditionException;
import com.wjb.blibli.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)//优先级
@Component
@Aspect//切面注解
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    //切点,在某个类被执行之前进行切入
    @Pointcut("@annotation(com.wjb.blibli.domain.annotation.DataLimited)")
    public void check(){

    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint) {
        //获取到userid然后去service里查用户的等级
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);

        //同时也将获取到的用户列表的用户roleid进行Set转换
        Set<String> roleCodeSet=userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());

        //获取到前段传来的数据,这里是UserMoment
        Object[] args = joinPoint.getArgs();
        //由于获取到的是数组所以我们需要进行循环
        for (Object arg : args) {
            //如果获取到的是UserMoment则进行操作校验
            if (arg instanceof UserMoment) {
                UserMoment userMoment = (UserMoment) arg;
                String type = userMoment.getType();
                //检查用户是否拥有权限,如果权限等于lv0并且操作类型只要不是0则抛出异常
                if (roleCodeSet.contains(AuthRoleConstant.ROLE_LV0) && !"0".equals(type)) {
                    throw new ConditionException("invalid args");
                }
            }
        }

    }
}
