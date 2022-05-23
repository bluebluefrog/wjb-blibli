package com.wjb.blibli.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JsonHttpMessageConverterConfig {
    @Bean
    @Primary
    //HttpMessageConverters是springboot对http方法请求数据做转换的工具类
    public HttpMessageConverters fastJsonHttpMessageConverter(){
        //实例化alibaba的Json处理器
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //实例化处理器配置
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //设置处理器时间格式
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //设置处理器序列化
        //设置格式化,无数据null改成empty,Map数据键值对排序,关闭循环引用(fastJson重复进行转化时循环引用会引用上次的引用地址)
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.MapSortField,
                SerializerFeature.DisableCircularReferenceDetect
        );
        //配置处理器
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return new HttpMessageConverters(fastConverter);
    }
}
