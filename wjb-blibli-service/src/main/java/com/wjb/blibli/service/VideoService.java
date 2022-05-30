package com.wjb.blibli.service;

import com.wjb.blibli.domain.*;
import org.apache.mahout.cf.taste.common.TasteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
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

    void addVideoComment(VideoComment videoComment, Long currentUserId);

    PageResult<VideoComment> getVideoComment(Integer size, Integer no, Long videoId);

    Map<String, Object> getVideoDetail(Long videoId);

    void addVideoView(VideoView videoView, HttpServletRequest request);

    Integer getVideoViewCounts(Long videoId);

    List<Video> recommend(Long currentUserId) throws TasteException;
}
