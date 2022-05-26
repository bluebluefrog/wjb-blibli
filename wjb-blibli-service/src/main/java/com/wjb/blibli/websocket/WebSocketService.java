package com.wjb.blibli.websocket;

import com.alibaba.fastjson.JSONObject;
import com.wjb.blibli.domain.Danmu;
import com.wjb.blibli.service.DanmuService;
import com.wjb.blibli.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.applet.AppletContext;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/imwebsocketserver/{token}")//如何找到websocket的路径 后面可以跟前端传来的数据 这里是token
public class WebSocketService {

    //获取logger(将当前logger所在的class 放在logger初始化当中 可以获取和该class相关的日志)
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //ONLINE_COUNT是当前连接人数数量
    //AtomicInteger是java提供的原子性操作得类 防止高并发操作时线程的不安全
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    //ConcurrentHashMap保证线程安全的map类 防止高并发操作时线程的不安全
    //Map通过保存每一个客户端的WebSocketService 高效获取WebSocket服务
    //Bean依赖注入是一个单例模式(只会生成一个实例 所有的类通用Bean)
    //WebSocketService不是一个单例模式 每一个客户端就要生成一个WebSocketService(对于多例模式就要使用map存储每一个客户端对应的 WebSocketService)
    private static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();

    //服务端和客户端通信的会话 用于长连接通信
    //服务端会保存客户端的session 需要推送消息的使用拿取session
    //通讯流程
    //1获取客户端的唯一标识从WEBSOCKET_MAP中获取WebSocketService
    //2WebSocketService保存了Session的变量 取得session
    //3调用session方法进行通信
    private Session session;

    //用于session唯一标识
    private String sessionId;

    //用于解决多例模式下@Autowired问题
    //注意需要使用static标识的变量才可以被多个连接使用
    //用于储存APPLICATION_CONTEXT全局上下文 ApplicationContext拥有SpringBoot帮我们生成好的所有Bean
    private static ApplicationContext APPLICATION_CONTEXT;

    //存储userId
    private Long userId;

    //多例模式下@Autowired引入类会有空指针异常
    //启动类后返回ApplicationContext传递给参数
    //由于不能使用@Autowired注入appcontext 在项目启动后给APPLICATION_CONTEXT设置变量
    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }


    //建立成功后获取session
    @OnOpen//连接成功时调用方法
    public void openConnection(Session session, @PathParam("token") String token) {
        //不登录也可以获得弹幕所以需要catch异常
        try {
            //解析用户token获取userId
            this.userId = TokenUtil.verifyToken(token);
        }catch(Exception e){

        }
        //获取sessionId 设置id和session
        this.sessionId = session.getId();
        this.session = session;

        //WEBSOCKET_MAP如果已经存在sessionId 去除再增加WebSocketService
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        } else {
            //若map中不存在则直接增加
            WEBSOCKET_MAP.put(sessionId, this);
            //增加在线人数1
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("user connect scussess:" + sessionId + "user online:" + ONLINE_COUNT.get());
        //告诉前端连接成功
        try {
            this.sendMessage("0");
        } catch (Exception e) {
            logger.error("connection error");
        }
    }



    @OnClose//关闭连接时调用
    public void closeConnection(){
        //移除map中的sessionId
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            //减少在线人数1
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("user logout:" + sessionId + "user online:" + ONLINE_COUNT.get());
    }

    @OnMessage//当有消息通信时
    public void onMessage(String message){
        logger.info("user info:" + sessionId + "massage:" + message);
        //判断message是否合法
        if (!StringUtil.isNullOrEmpty(message)) {
            try{
                //群发消息
                //遍历map中的每个WebSocketService
                for (Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()) {
                    WebSocketService webSocketService = entry.getValue();
                    //判断session是否断开
                    if (webSocketService.session.isOpen()) {
                        webSocketService.sendMessage(message);
                    }
                }
                //userId不为空才可以添加弹幕
                if (this.userId != null) {
                    //保存弹幕到数据库
                    //将message转换成Danmu类
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    //设置danmu基本值
                    danmu.setId(userId);
                    danmu.setCreateTime(new Date());
                    //通过APPLICATION_CONTEXT获取danmuServiceBean 因为是多例所以不能使用@Autowired
                    DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.addDanmu(danmu);
                    //保存弹幕到redis
                    danmuService.addDanmusToRedis(danmu);
                }
            }catch(Exception e){
                logger.error("danmu recived error");
                e.printStackTrace();
            }
        }
    }

    @OnError//当有ERROR时
    public void onError(Throwable error){

    }

    private void sendMessage(String message) throws IOException {
        //使用session发送消息
        this.session.getBasicRemote().sendText(message);
    }
}
