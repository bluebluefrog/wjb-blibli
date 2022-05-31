package com.wjb.blibli.service;

import com.wjb.blibli.domain.*;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.bytedeco.javacv.FrameGrabber;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    //将偏好数据输入模型 可以在模型当中存储相关的偏好数据得分
    DataModel createDataModel(List<UserPreference> userPreferenceList);

    //基于内容的协同推荐
    //@param userId 用户id
    //@param itemId 参考内容id（根据该内容进行相似内容推荐）
    //@param howMany 需要推荐的数量
    List<Video> recommendByItem(Long userId, Long itemId, int howMany) throws TasteException;

    List<VideoBinaryPicture> convertVideoToImage(Long videoId, String fileMd5) throws Exception;
}
