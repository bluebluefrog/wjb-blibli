package com.wjb.blibli.service.impl;

import com.wjb.blibli.domain.Video;
import com.wjb.blibli.repository.VideoRepository;
import com.wjb.blibli.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private VideoRepository videoRepository;


    //添加需要查询的投稿视频
    public void addVideo(Video video) {
        //使用es的repo进行添加
        videoRepository.save(video);
    }

    //查询的投稿视频
    public Video getVideos(String keyword) {
        //使用es的repo进行查询 使用keyword
        Video video = videoRepository.findByTitleLike(keyword);

        return video;
    }

    //删除所有视频
    public void deleteVideos(){
        videoRepository.deleteAll();
    }
}
