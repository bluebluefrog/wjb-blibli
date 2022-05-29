package com.wjb.blibli.controller.support;

import com.wjb.blibli.domain.Danmu;
import com.wjb.blibli.domain.JsonResponse;
import com.wjb.blibli.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DanmuController {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private DanmuService danmuService;


    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>> getDanmus(@RequestParam Long videoId,String startTime,String endTime) throws Exception {
        List<Danmu> danmuList;
        try{
            //判断当前是游客模式还是用户登录模式
            userSupport.getCurrentUserId();
            //若是用户登录模式 允许用户进行时间段筛选
            danmuList = danmuService.getDanmus(videoId, startTime, endTime);
        }catch(Exception e){
            //游客模式不能筛选
            danmuList = danmuService.getDanmus(videoId, null, null);
        }
        return new JsonResponse<>(danmuList);
    }
}
