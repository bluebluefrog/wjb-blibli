package com.wjb.blibli.controller.aspact;

import com.wjb.blibli.controller.support.UserSupport;
import com.wjb.blibli.domain.annotation.ApiLimitedRole;
import com.wjb.blibli.domain.auth.UserRole;
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
public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    //切点,在某个类被执行之前进行切入
    @Pointcut("@annotation(com.wjb.blibli.domain.annotation.ApiLimitedRole)")
    public void check(){

    }

    //在执行某方法之前进行这里是切点方法check()
    //before注解的方法增加参数,需要在注解中指定这里使用了针对注解@annotation(apiLimitedRole)获取apiLimitedRole对象
    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        //获取到userid然后去service里查用户的等级
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //完成查询后获取到被限制的用户等级列表
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        //将被限制的用户等级列表进行Set的转换,方便做交集测试
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        //同时也将获取到的用户列表的用户roleid进行Set转换
        Set<String> roleCodeSet=userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        //交集测试,如果被限制的角色列表里面包含该用户角色id则交集应该大于0,此时说明该用户没有权限
        roleCodeSet.retainAll(limitedRoleCodeSet);
        if (roleCodeSet.size() > 0) {
            throw new ConditionException("no permission!");
        }
    }
}
