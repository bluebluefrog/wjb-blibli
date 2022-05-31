package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.VideoDao;
import com.wjb.blibli.domain.*;
import com.wjb.blibli.domain.exception.ConditionException;
import com.wjb.blibli.service.FileService;
import com.wjb.blibli.service.UserCoinService;
import com.wjb.blibli.service.UserService;
import com.wjb.blibli.service.VideoService;
import com.wjb.blibli.util.FastDFSUtil;
import com.wjb.blibli.util.ImageUtil;
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
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private ImageUtil imageUtil;

    @Autowired
    private UserCoinService userCoinService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    private static final int FRAME_NO = 256;

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

    //基于用户推荐
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
    public DataModel createDataModel(List<UserPreference> userPreferenceList) {
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

    public List<VideoBinaryPicture> convertVideoToImage(Long videoId, String fileMd5) throws Exception {
        //根据文件md5查询相关视频文件
        File fileByMd5 = fileService.getFileByMd5(fileMd5);
        //当有多个服务器分布式时 单机视频处理会出现问题所以需要先下载
        //本地下载路径
        String filePath = "/Users/hat/tmpfile/fileForVideoId" + videoId + "." + fileByMd5.getType();
        //根据数据库中视频的url下载文件到本地
        fastDFSUtil.downLoadFile(fileByMd5.getUrl(),filePath);
        //FFmpegFrameGrabber在javacv中使用 用于截取视频帧的类
        //通过文件的路径生成相关实体类参数1路径
        FFmpegFrameGrabber fFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(filePath);
        //调用开始方法
        fFmpegFrameGrabber.start();
        //获取视频中总帧数
        int ffLength = fFmpegFrameGrabber.getLengthInFrames();
        //新建帧 用于后面储存帧 每一帧重新赋值
        Frame frame;
        //转换器 将截取的帧进行内容转换 用于把帧转换成文件类
        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
        //用于记录什么时候需要截取帧进行图片转换 每256帧进行一次转换 节省性能不至于每一帧都需要转换
        int count = 1;
        //用于存储转换完的黑白图片
        List<VideoBinaryPicture> pictureList = new ArrayList<>();
        //对每一帧进行遍历
        for (int i = 0; i < ffLength; i++) {
            //获取当前帧时间戳
            long timestamp = fFmpegFrameGrabber.getTimestamp();
            //获取当前帧图片
            frame = fFmpegFrameGrabber.grabImage();
            //当count(用于记录哪帧需要被截取的变量)等于i(当前帧)时截取
            if (count == i) {
                //当前帧图片不存在报错
                if (frame == null) {
                    throw new ConditionException("illgel frame");
                }
                //利用转换器将帧图片转换成BufferedImage(一种图片的数据形式).getBufferedImage()参数Frame
                BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);
                //新建一个ByteArrayOutputStream输出流
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                //将转换过的帧图片img传入输出到本地文件png格式
                //.write()参数1RenderedImage实现类参数2格式参数3输出流
                ImageIO.write(bufferedImage, "png", os);
                //将输出流转化成输入流 在黑白剪影图片方法中需要用到输入流
                ByteArrayInputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                //输出黑白剪影文件
                //指定临时文件 方便文件输出
                java.io.File outputFile = java.io.File.createTempFile("convert-" + videoId + "-" , ".png");
                //将文件转换成黑白剪影
                BufferedImage binaryImage = imageUtil.getBodyOutline(bufferedImage, inputStream);
                ImageIO.write(binaryImage, "png", outputFile);
                //有的浏览器或网站需要把图片白色的部分转为透明色，使用以下方法可实现
                imageUtil.transferAlpha(outputFile, outputFile);
                //上传视频剪影文件
                //将视频上传到fastDFS
                String imgUrl = fastDFSUtil.uploadCommFile(outputFile, "png");
                //设置储存到数据库的值
                VideoBinaryPicture videoBinaryPicture = new VideoBinaryPicture();
                videoBinaryPicture.setFrameNo(i);
                videoBinaryPicture.setUrl(imgUrl);
                videoBinaryPicture.setVideoId(videoId);
                videoBinaryPicture.setVideoTimestamp(timestamp);
                pictureList.add(videoBinaryPicture);
                //count+256
                count += FRAME_NO;
                //删除临时文件
                outputFile.delete();
            }
        }
        //删除临时文件
        java.io.File tmpFile = new java.io.File(filePath);
        tmpFile.delete();
        //批量添加视频剪影文件
        videoDao.batchAddVideoBinaryPictures(pictureList);
        return pictureList;
    }
}
