package com.wjb.blibli.util;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.CountDownLatch2;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.TimeUnit;

public class RocketMQUtil {

    //同步发送
    //关心消息发送成功失败
    public static void syncSendMsg(DefaultMQProducer producer, Message msg) throws Exception {
        SendResult result = producer.send(msg);
        System.out.println(result);
    }
    //异步发送
    //不关心消息发送成功失败
    public static void asyncSendMsg(DefaultMQProducer producer, Message msg) throws Exception {
        //计数，发送几次
        int messageCount=2;
        CountDownLatch2 countDownLatch = new CountDownLatch2(messageCount);
        for (int i = 0; i < messageCount; i++) {
            producer.send(msg, new SendCallback() {
                //发送成功回调
                @Override
                public void onSuccess(SendResult sendResult) {
                    countDownLatch.countDown();
                    System.out.println(sendResult.getMsgId());
                }
                //发送失败回调
                @Override
                public void onException(Throwable e) {
                    countDownLatch.countDown();
                    System.out.println("发送消息发生了异常！" + e);
                    e.printStackTrace();
                }
            });
        }
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}
