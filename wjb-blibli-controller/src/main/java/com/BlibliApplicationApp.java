package com;

import com.wjb.blibli.websocket.WebSocketService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement//开启事务
@EnableAsync//开启异步
@EnableScheduling//开启定时任务
@MapperScan(basePackages = "com.wjb.blibli.dao")
public class BlibliApplicationApp {

    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(BlibliApplicationApp.class, args);
        //用于解决多例@Autowired问题
        WebSocketService.setApplicationContext(app);

    }
}
