package com.wjb.blibli.service;

import com.wjb.blibli.domain.Danmu;

import java.util.List;

public interface DanmuService {

    void addDanmu(Danmu danmu);

    void asyncAddDanmu(Danmu danmu);

    List<Danmu> getDanmus(Long videoId,
                          String startTime, String endTime) throws Exception;

    void addDanmusToRedis(Danmu danmu);
}
