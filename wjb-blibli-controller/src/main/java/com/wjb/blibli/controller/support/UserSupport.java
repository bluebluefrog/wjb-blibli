package com.wjb.blibli.controller.support;

import com.wjb.blibli.domain.exception.ConditionException;
import com.wjb.blibli.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
@Component
public class UserSupport {
    public Long getCurrentUserId() {
        //抓取请求request上下文
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        //获取token
        String token = requestAttributes.getRequest().getHeader("token");
        Long userId = TokenUtil.verifyToken(token);
        if (userId < 0) {
            throw new ConditionException("非法用户!");
        }
        return userId;
    }
}
