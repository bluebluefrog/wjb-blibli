package com.wjb.blibli.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

//继承AbstractElasticsearchConfiguration
@Configuration
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

    //es地址
    @Value("${elasticsearch.url}")
    private String esUrl;

    //RestHighLevelClient是es提供的高端访问esRestApi客户端
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        //创建ClientConfiguration 传入esurl
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(esUrl).build();

        //使用RestClients.create(传入ClientConfiguration)生成api客户端
        //.rest()高版本.lowLevelRest()低版本
        return RestClients.create(clientConfiguration).rest();
    }
}
