package com.wjb.blibli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    //ServerEndpointExporter服务器端点导出者
    //将ServerEndpoint进行发现导出(使用WebSocket前置条件)
    @Bean
    public ServerEndpointExporter severEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
