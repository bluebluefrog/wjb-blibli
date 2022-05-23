package com.wjb.blibli.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wjb.blibli.dao.UserMomentDao;
import com.wjb.blibli.domain.UserMoment;
import com.wjb.blibli.domain.constant.UserMomentsConstant;
import com.wjb.blibli.service.UserMomentService;
import com.wjb.blibli.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class UserMomentServiceImpl implements UserMomentService {

    @Autowired
    private UserMomentDao userMomentDao;

    //用来获取RocketMq生产者消费者
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void addUserMoment(UserMoment userMoment) throws Exception{
    userMoment.setCreateTime(new Date());
        userMomentDao.addUserMoment(userMoment);
        //获取生产者
        DefaultMQProducer producer =(DefaultMQProducer) applicationContext.getBean(("momentsProducer"));
        //新建一个消息,传入userMoments(只能接收bytes数组使用JSONObject.toJSONString进行转换)
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        //同步发送
        RocketMQUtil.syncSendMsg(producer,msg);

    }

    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        String key="subscribed-" + userId;
        //从redis中取出json格式动态list
        String listStr= redisTemplate.opsForValue().get(key);
        //将listStr转译成list<UserMoment>
        return JSONArray.parseArray(listStr,UserMoment.class);
    }
}
