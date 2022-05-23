package com.wjb.blibli.domain.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)//Retention(保留)注解说明,这种类型的注解会被保留到那个阶段
// 注解在运行阶段
@Target({ElementType.METHOD})//Target说明了Annotation所修饰的对象范围(类、接口、枚举、Annotation类型)
@Documented//@Documented 是一个标记注解，没有成员变量 用@Documented 注解修饰的注解类会被JavaDoc 工具提取成文档 默认情况下，JavaDoc 是不包括注解的，但如果声明注解时指定了@Documented，就会被JavaDoc 之类的工具处理，所以注解类型信息就会被包括在生成的帮助文档中
@Component
public @interface ApiLimitedRole {

    //角色唯一编码
    //从外面传值,在使用接口时
    //limitedRoleCodeList存储了不具有权限调用该接口的角色
    String[] limitedRoleCodeList() default{};

}
