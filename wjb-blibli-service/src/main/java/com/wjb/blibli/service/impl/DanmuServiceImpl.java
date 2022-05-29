package com.wjb.blibli.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wjb.blibli.dao.DanmuDao;
import com.wjb.blibli.domain.Danmu;
import com.wjb.blibli.service.DanmuService;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DanmuServiceImpl implements DanmuService {

    private static final String DANMU_KEY = "dm-video-";

    @Autowired
    private DanmuDao danmuDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void addDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
    }


    @Async
    public void asyncAddDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
    }


    //查询策略是优先查redis中的弹幕数据，
    //如果redis没有的话查询数据库，然后把查询的数据写入redis当中
    public List<Danmu> getDanmus(Long videoId, String startTime, String endTime) throws Exception {
        //拼接redis key
        //redis key样式dm-video-videoId
        String key = DANMU_KEY + videoId;
        //获取redis中的弹幕
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu> list;
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
            //将redis的值进行时间的筛选
            if (!StringUtil.isNullOrEmpty(startTime)
                && !StringUtil.isNullOrEmpty(endTime)) {
                //格式化传入的date然后进行比较 筛选出在范围之内的danmus
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = simpleDateFormat.parse(startTime);
            Date endDate = simpleDateFormat.parse(endTime);
            List<Danmu> danmusInTimeZoneList = new ArrayList<>();
                for (Danmu damu : list) {
                    Date createTime = damu.getCreateTime();
                    if(createTime.after(startDate)&& createTime.before(endDate)){
                        danmusInTimeZoneList.add(damu);
                    }

                }
                list = danmusInTimeZoneList;
        }
        }else{
            //redis中没有数据
            Map<String, Object> params = new HashMap<>();
            params.put("videoId", videoId);
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            //查询完后保存到redis
            list = danmuDao.getDanmus(params);
            redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));

        }
        return list;
    }


    public void addDanmusToRedis(Danmu danmu) {
        //拼接redis key
        //redis key样式dm-video-videoId
        String key = DANMU_KEY + danmu.getVideoId();
        //获取redis中的弹幕
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu> danmuList = new ArrayList<>();
        //如果不为空则转成Danmu数组 并使用已经存在的数组
        if (!StringUtil.isNullOrEmpty(value)) {
            danmuList = JSONArray.parseArray(value, Danmu.class);
        }
        //进行新的弹幕添加
        danmuList.add(danmu);
        //redis存储
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(danmu));
    }
}
