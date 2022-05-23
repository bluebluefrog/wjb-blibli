package com.wjb.blibli.dao;

import com.wjb.blibli.domain.Video;
import com.wjb.blibli.domain.VideoTag;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface VideoDao {

    Integer addVideos(Video video);

    //添加视频相关标签
    Integer batchAddVideoTags(List<VideoTag> videoTagList);

    Integer pageCountVideos(Map<String,Object> params);

    List<Video> pageListVideos(Map<String, Object> params);
}
