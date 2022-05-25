package com.wjb.blibli.service;

import com.wjb.blibli.domain.PageResult;
import com.wjb.blibli.domain.Video;
import com.wjb.blibli.domain.VideoCoin;
import com.wjb.blibli.domain.VideoCollection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface VideoService {
    void addVideo(Video video);

    PageResult<Video> pageListVideos(Integer size, Integer no, String area);

    void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception;

    void addVideoLike(Long videoId, Long userId);

    void deleteVideoLike(Long videoId, Long userId);

    Map<String, Object> getVideoLikes(Long videoId, Long userId);

    Map<String, Object> getVideoCollection(Long videoId, Long currentUserId);

    void deleteVideoCollection(Long videoId, Long currentUserId);

    void addVideoCollection(VideoCollection videoCollection, Long currentUserId);

    Map<String, Object> getVideoCoins(Long videoId, Long currentUserId);

    void addVideoCoins(VideoCoin videoCoin, Long currentUserId);
}
