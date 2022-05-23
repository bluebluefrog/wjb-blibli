package com.wjb.blibli.handler;

import com.wjb.blibli.domain.JsonResponse;
import com.wjb.blibli.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
//优先级最高的处理器按最优先处理
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    //用于处理Exception
    @ExceptionHandler(value =Exception.class)
    //返回一个body
    @ResponseBody
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request, Exception e) {
        //获取错误信息
        String errorMsg = e.getMessage();
        //如果错误信息属于ConditionException则我们还可以获取状态码
        if(e instanceof ConditionException){
            String errorCode=((ConditionException)e).getCode();
            return new JsonResponse<>(errorCode, errorMsg);
        }else{
            return new JsonResponse<>("500",errorMsg);
        }

    }
}
