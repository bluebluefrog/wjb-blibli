package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.VideoDao;
import com.wjb.blibli.domain.*;
import com.wjb.blibli.domain.exception.ConditionException;
import com.wjb.blibli.service.UserCoinService;
import com.wjb.blibli.service.UserService;
import com.wjb.blibli.service.VideoService;
import com.wjb.blibli.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private UserCoinService userCoinService;

    @Autowired
    private UserService userService;

    @Transactional
    public void addVideo(Video video) {
        //添加视频
        Date now = new Date();
        video.setCreateTime(now);
        videoDao.addVideos(video);
        Long videoId = video.getId();
        List<VideoTag> tagList = video.getVideoTagList();
        //批量设置tag属性
        tagList.forEach(item -> {
            item.setCreateTime(now);
            item.setVideoId(videoId);
        });
        //添加到video_tag表
        videoDao.batchAddVideoTags(tagList);
    }


    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if (size == null || no == null) {
            throw new ConditionException("param wrong");
        }
        Map<String, Object> params = new HashMap<>();
        //起始查询数据
        params.put("start", (no - 1) * size);
        //查多少条
        params.put("limit", size);
        //分类
        params.put("area", area);
        List<Video> list = new ArrayList<>();
        //查出满足area条件的数据数量
        Integer total = videoDao.pageCountVideos(params);
        //如果有视频才具体查出视频信息
        if (total > 0) {
            list = videoDao.pageListVideos(params);
        }

        return new PageResult<>(total, list);
    }


    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        fastDFSUtil.viewVideoOnlineBySlices(request, response, url);
    }

    public void addVideoLike(Long videoId, Long userId) {
        //检查视频是否存在
        Video video = videoDao.getVideoById(videoId);
        if(video == null){
            throw new ConditionException("illgel video！");
        }
        //检查是否已经点赞
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        if(videoLike != null){
            throw new ConditionException("like already！");
        }
        //新增点赞
        videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId, userId);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        //获取视频总点赞数
        Long count = videoDao.getVideoLikes(videoId);
        //获取用户是否点赞
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        //已点赞赋值true
        boolean like = videoLike != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }

    public Map<String, Object> getVideoCollection(Long videoId, Long currentUserId) {
        //获得总收藏量
        Long count = videoDao.getVideoCollections(videoId);
        //获得用户是否收藏
        VideoCollection videoCollection = videoDao.getVideoCollectionByVideoIdAndUserId(videoId, currentUserId);
        //已收藏赋值true
        boolean like = videoCollection != null;
        HashMap<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);

        return result;
    }

    public void deleteVideoCollection(Long videoId, Long currentUserId) {
        videoDao.deleteVideoCollection(videoId, currentUserId);
    }

    @Transactional
    public void addVideoCollection(VideoCollection videoCollection, Long currentUserId) {
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();
        //参数校验
        if (videoId == null || groupId == null) {
            throw new ConditionException("bad param!");
        }
        Video videoById = videoDao.getVideoById(videoId);
        if (videoById == null) {
            throw new ConditionException("illgel video!");
        }
        //先删除后添加
        //删除
        videoDao.deleteVideoCollection(videoId, currentUserId);
        //添加
        videoCollection.setUserId(currentUserId);
        videoCollection.setCreateTime(new Date());
        videoDao.addVideoCollection(videoCollection);
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long currentUserId) {
        //获取总投币数
        Long count = videoDao.getVideoCoinsAmount(videoId);
        //获取用户是否投币
        VideoCoin videoCoins = videoDao.getVideoCoinByVideoIdAndUserId(videoId, currentUserId);
        boolean like = videoCoins != null;
        HashMap<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);

        return result;
    }

    @Transactional
    public void addVideoCoins(VideoCoin videoCoin, Long currentUserId) {
        Long videoId = videoCoin.getVideoId();
        Integer amount = videoCoin.getAmount();
        //检查视频是否存在
        if (videoId == null) {
            throw new ConditionException("param error!");
        }
        Video videoById = videoDao.getVideoById(videoId);
        if (videoById == null) {
            throw new ConditionException("illgel video!");
        }
        //查询用户是否有足够硬币
        Integer userCoinsAmount = userCoinService.getUserCoinsAmount(currentUserId);
        userCoinsAmount = userCoinsAmount == null ? 0 : userCoinsAmount;
        if (amount > userCoinsAmount) {
            throw new ConditionException("coin not enough!");
        }
        //查询当前用户对食品投了多少币
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByVideoIdAndUserId(videoId, currentUserId);
        //新增视频投币
        if (dbVideoCoin == null) {
            videoCoin.setUserId(currentUserId);
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        }else{
            Integer dbAmount = dbVideoCoin.getAmount();
            dbAmount+=amount;
            //更新视频投币
            videoCoin.setUserId(currentUserId);
            videoCoin.setAmount(dbAmount);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        //更新用户硬币总数
        userCoinService.updateUserCoinsAmount(currentUserId, (userCoinsAmount - amount));

    }

    @Transactional
    public void addVideoComment(VideoComment videoComment, Long currentUserId) {
        //添加视频评论(可以额外验证如果rootId存在 查询一下db中是否存在上层节点)
        Long videoId = videoComment.getVideoId();
        if (videoId == null) {
            throw new ConditionException("param error!");
        }
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("illgel video!");
        }
        videoComment.setUserId(currentUserId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    public Map<String, Object> getVideoComment(Integer size, Integer no, Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("illgel video!");
        }
        //设置分页查询
        HashMap<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("videoId", videoId);
        Integer total = videoDao.pageCountVideoComments(params);
        List<VideoComment> comments = new ArrayList<>();
        //总数大于0则进行
        if (total > 0) {
            //查询以及评论
            comments = videoDao.pageListVideoComments(params);
            //筛选出一级评论的id
            List<Long> parentIdList = comments.stream().map(VideoComment::getId).collect(Collectors.toList());
            //根据一级评论id查询二级评论
            List<VideoComment> childComments = videoDao.batchGetVideoCommentsByRootIds(parentIdList);
            //筛选一级评论用户id
            Set<Long> parentUserId = comments.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            //筛选二级评论用户id
            Set<Long> childUserId = childComments.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            //获取视频发布用户UserId
            Set<Long> userIdList = new HashSet<>();
            userIdList.add(video.getUserId());
            userIdList.addAll(parentUserId);
            userIdList.addAll(childUserId);
            //查询用户信息一级二级评论用户和视频发布用户一起查询
            userService.


            userService.getUserInfoByIds(parentIdList);
        }
        return null;
    }
}
