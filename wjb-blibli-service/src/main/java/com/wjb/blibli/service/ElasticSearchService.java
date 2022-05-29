package com.wjb.blibli.service;

import com.wjb.blibli.domain.Video;

public interface ElasticSearchService {
    //添加需要查询的投稿视频
    void addVideo(Video video);

    //查询的投稿视频
    Video getVideos(String keyword);

    //删除所有视频
    void deleteVideos();
}
