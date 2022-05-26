package com;

import com.wjb.blibli.websocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BlibliApplicationApp {

    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(BlibliApplicationApp.class, args);
        //用于解决多例@Autowired问题
        WebSocketService.setApplicationContext(app);

    }
}
