package com.wjb.blibli.service.impl;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.wjb.blibli.dao.VideoDao;
import com.wjb.blibli.domain.*;
import com.wjb.blibli.domain.exception.ConditionException;
import com.wjb.blibli.service.UserCoinService;
import com.wjb.blibli.service.UserService;
import com.wjb.blibli.service.VideoService;
import com.wjb.blibli.util.FastDFSUtil;
import com.wjb.blibli.util.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
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
        //添加视频评论(可以额外验证如果rootId或repleyId存在 查询一下db中是否存在上层节点)
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

    public PageResult<VideoComment> getVideoComment(Integer size, Integer no, Long videoId) {
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
            Set<Long> userIdList = comments.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            //筛选二级评论用户id
            Set<Long> childUserId = childComments.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            //筛选二级评论回复的用户id
            Set<Long> replyUserId = childComments.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            //将所有id添加到一个set进行查询
            userIdList.addAll(childUserId);
            userIdList.addAll(replyUserId);

            //查询用户信息一级二级评论用户和二级评论回复的用户一起查询
            List<UserInfo> userInfos = userService.batchGetUserInfoByUserIds(userIdList);
            Map<Long, UserInfo> userInfoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));

            //循环比较哪些二级评论的rootId是以及评论的id
            comments.forEach(comment->{
                Long id = comment.getId();
                List<VideoComment> childList = new ArrayList<>();
                childComments.forEach(childComment ->{
                    if (id.equals(childComment.getRootId())) {
                        //如果是则通过user设置用户info
                        childComment.setUserInfo(userInfoMap.get(childComment.getUserId()));
                        childComment.setReplyUserInfo(userInfoMap.get(childComment.getReplyUserId()));
                        childList.add(childComment);
                    }
                });
                comment.setChildList(childList);
                comment.setUserInfo(userInfoMap.get(comment.getUserId()));
            });

        }
        return new PageResult<>(total,comments);
    }

    public Map<String, Object> getVideoDetail(Long videoId) {
        Video video = videoDao.getVideoDetails(videoId);
        Long userId = video.getUserId();
        User user = userService.getUserInfo(userId);
        UserInfo userInfo = user.getUserInfo();
        HashMap<String, Object> result = new HashMap<>();
        result.put("video", video);
        result.put("userInfo", userInfo);
        return result;
    }

    public void addVideoView(VideoView videoView, HttpServletRequest request) {
        Long userId = videoView.getUserId();
        Long videoId = videoView.getVideoId();
        //通过UserAgent生成clientId
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        //使用IpUtil获取ip
        String ip = IpUtil.getIP(request);
        Map<String, Object> params = new HashMap<>();
        //登录查询条件依据userId 不登录查询条件依据clientId和ip
        if (userId != null) {
            params.put("userId",userId);
        }else{
            params.put("ip", ip);
            params.put("clientId", clientId);
        }
        //将当前日期格式化进行查询对比
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        params.put("today", sdf.format(now));
        params.put("videoId", videoId);
        //添加观看记录
        //查询是否存在 不存在则添加
        VideoView dbVideoView = videoDao.getVideoView(params);
        if (dbVideoView == null) {
            videoView.setIp(ip);
            videoView.setClientId(clientId);
            videoView.setCreateTime(new Date());
            videoDao.addVideoView(videoView);
        }
    }

    public Integer getVideoViewCounts(Long videoId){
        return videoDao.getVideoViewCounts(videoId);
    }

    public List<Video> recommend(Long currentUserId) throws TasteException {
        List<UserPreference> allUserPreference = videoDao.getAllUserPreference();
        //创建数据模型 mahout提供的数据分析模型
        DataModel dataModel = this.createDataModel(allUserPreference);
        //计算dataModel中用户的相似程度
        //创建UserSimilarity进行分析 这里使用UncenteredCosineSimilarity基于余弦相似度计算
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        //获取用户邻居数据 将最接近的用户取出
        //创建UserNeighborhood 这里使用NearestNUserNeighborhood最接近的用户 传入参数1邻居数量 2相似度UserSimilarity 3DataModel
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
        //构建推荐器
        //使用GenericUserBasedRecommender基于用户的推荐 参数1DataModel 2UserNeighborhood 3相似度UserSimilarity
        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);

        //使用推荐器推荐物品
        //recommend参数1基于什么推荐2需要推荐的物品数量
        List<RecommendedItem> recommendedItems = recommender.recommend(currentUserId, 5);
        //将物品id抽取
        List<Long> itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        //搜索video进行返回
        return videoDao.batchGetVideosByIds(itemIds);
    }


    //基于内容的协同推荐
    //@param userId 用户id
    //@param itemId 参考内容id（根据该内容进行相似内容推荐）
    //@param howMany 需要推荐的数量
    public List<Video> recommendByItem(Long userId, Long itemId, int howMany) throws TasteException {
        List<UserPreference> list = videoDao.getAllUserPreference();
        //创建数据模型
        DataModel dataModel = this.createDataModel(list);
        //获取内容相似程度
        ItemSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        GenericItemBasedRecommender genericItemBasedRecommender = new GenericItemBasedRecommender(dataModel, similarity);
        // 物品推荐相拟度，计算两个物品同时出现的次数，次数越多任务的相拟度越高
        List<Long> itemIds = genericItemBasedRecommender.recommendedBecause(userId, itemId, howMany)
                .stream()
                .map(RecommendedItem::getItemID)
                .collect(Collectors.toList());
        //推荐视频
        return videoDao.batchGetVideosByIds(itemIds);
    }

    //将偏好数据输入模型 可以在模型当中存储相关的偏好数据得分
    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        //通过lambda对用户偏好进行分组 转换成map形式userid对应用户偏好得分
        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
        //提取每个用户的偏好列表
        Collection<List<UserPreference>> list = map.values();
        for(List<UserPreference> userPreferences : list){
            //将偏好内容进行遍历 把每一个元素换成GenericPreference推荐分析
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for(int i = 0; i < userPreferences.size(); i++){
                UserPreference userPreference = userPreferences.get(i);
                //给推荐引擎GenericPreference进行赋值操作 生成新的实体类
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                array[i] = item;
            }
            //将其放到数组中
            //前面是用户id 后面是用户偏好物品
            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIdMap);
    }
}
