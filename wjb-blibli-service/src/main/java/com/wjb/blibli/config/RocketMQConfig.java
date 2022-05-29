package com.wjb.blibli.config;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wjb.blibli.domain.Danmu;
import com.wjb.blibli.domain.UserFollowing;
import com.wjb.blibli.domain.UserMoment;
import com.wjb.blibli.domain.constant.UserDanmusConstant;
import com.wjb.blibli.domain.constant.UserMomentsConstant;
import com.wjb.blibli.service.DanmuService;
import com.wjb.blibli.service.UserFollowingService;
import com.wjb.blibli.websocket.WebSocketService;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RocketMQConfig {



    @Value("${rocketmq.name.server.address}")//在properties中进行设置服务器地址
    private String nameServerAddr;

    //redis工具类
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //用来获取用户粉丝
    @Autowired
    private UserFollowingService userFollowingService;

    @Autowired
    private DanmuService danmuService;

    //创建生产者
    //DefaultMQProducer是rocket自带的实体类
    @Bean("momentsProducer")
    public DefaultMQProducer momentProducer() throws Exception {
        //创建一个新的producer,传入MQ分组的命名
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        //设置地址
        producer.setNamesrvAddr(nameServerAddr);
        //启动
        producer.start();
        return producer;
    }

    //创建消费者
    //DefaultMQPushConsumer是使用推送方式进行消费
    @Bean("momentConsumer")
    public DefaultMQPushConsumer momentConsumer() throws Exception {
        //创建一个新的consumer,传入MQ分组的命名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        //设置地址
        consumer.setNamesrvAddr(nameServerAddr);
        //订阅生产者,传入订阅的一级主题,和二级主题
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS, "*");
        //注册信息监听
        //并发事件监听MessageListenerConcurrently
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            //实现方法
            //List<MessageExt> msgs 获取到的消息
            //ConsumeConcurrentlyContext存放一些处理相关的信息
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                if (msg == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String bodyStr = new String(msg.getBody());
                //将UserMoment的Bytes数组转回UserMoment实体
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr),UserMoment.class);
                Long userId = userMoment.getUserId();
                //获取到用户有哪些粉丝
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                for (UserFollowing fan : fanList) {
                    //用户从redis里查询用户的动态关注列表
                    String key="subscribed-" + fan.getUserId();
                    String subscribedListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subscribedList;
                    //如果列表不存在创建新列表
                    if(StringUtil.isNullOrEmpty(subscribedListStr)){
                        subscribedList = new ArrayList<>();

                    }else{
                        //如果列表存在
                        //将刚才获取到的列表Json字符格式转回List<UserMoment>
                        subscribedList = JSONArray.parseArray(subscribedListStr,UserMoment.class);
                    }
                    //将userMoment加入该用户的关注列表
                    subscribedList.add(userMoment);
                    //在redis里进行存储新的动态列表,Json格式
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList));
                }
                //消息消费状态
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("danmusSaveProducer")
    public DefaultMQProducer danmusSaveProducer() throws Exception {
        //通过分组创建消息生产者producer
        DefaultMQProducer producer = new DefaultMQProducer(UserDanmusConstant.GROUP_SAVE_DANMUS);
        //设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddr);
        //启动producer实例
        producer.start();
        return producer;
    }

    @Bean("danmusSaveConsumer")
    public DefaultMQPushConsumer danmusSaveConsumer() throws Exception {
        //通过分组实例化消费者consumer
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserDanmusConstant.GROUP_SAVE_DANMUS);
        //设置NameServer的地址
        consumer.setNamesrvAddr(nameServerAddr);
        //订阅一个或多个Topic 以及Tag来过滤需要消费的消息
        consumer.subscribe(UserDanmusConstant.TOPIC_SAVE_DANMUS, "*");

        //注册信息监听 注册回调实现类来处理从broker拉取回来的消息
        //并发事件监听MessageListenerConcurrently
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                //获取队列中第一个消息
                MessageExt msg = msgs.get(0);
                //将消息转换成byte数组
                byte[] msgByte = msg.getBody();
                //将消息bytes转换成string
                String msgBodyStr = new String(msgByte);
                //将消息string转换成jsonObj
                JSONObject msgJsonObject = JSONObject.parseObject(msgBodyStr);
                //将jsonObj转换成Danmu实体
                Danmu danmu = JSONObject.toJavaObject(msgJsonObject, Danmu.class);

                //优化异步保存弹幕
                danmuService.asyncAddDanmu(danmu);

                //消息被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }
    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception {
        //通过分组创建消息生产者producer
        DefaultMQProducer producer = new DefaultMQProducer(UserDanmusConstant.GROUP_DANMUS);
        //设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddr);
        //启动producer实例
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception {
        //通过分组实例化消费者consumer
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserDanmusConstant.GROUP_DANMUS);
        //设置NameServer的地址
        consumer.setNamesrvAddr(nameServerAddr);
        //订阅一个或多个Topic 以及Tag来过滤需要消费的消息
        consumer.subscribe(UserDanmusConstant.TOPIC_DANMUS, "*");

        //注册信息监听 注册回调实现类来处理从broker拉取回来的消息
        //并发事件监听MessageListenerConcurrently
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                //获取队列中第一个消息
                MessageExt msg = msgs.get(0);
                //将消息转换成byte数组
                byte[] msgByte = msg.getBody();
                //将消息bytes转换成string
                String msgBodyStr = new String(msgByte);
                //将消息string转换成jsonObj
                JSONObject msgJsonObject = JSONObject.parseObject(msgBodyStr);
                //获取消息中的sessionId和弹幕message
                String sessionId = msgJsonObject.getString("sessionId");
                String message = msgJsonObject.getString("message");
                //使用websocketService的map用sessionId获取当前客户端webSocketService
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                //有客户端连接则发送消息
                if (webSocketService.getSession().isOpen()) {
                    try{
                        webSocketService.sendMessage(message);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                //消息被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }
}
