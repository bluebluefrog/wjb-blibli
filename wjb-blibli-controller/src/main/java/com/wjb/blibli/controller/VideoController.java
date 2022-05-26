package com.wjb.blibli.controller;

import com.wjb.blibli.controller.support.UserSupport;
import com.wjb.blibli.domain.*;
import com.wjb.blibli.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserSupport userSupport;

    @PostMapping("/videos")
    public JsonResponse<String> addVideos(@RequestBody Video video) {
        Long currentUserId = userSupport.getCurrentUserId();
        video.setUserId(currentUserId);
        videoService.addVideo(video);
        return JsonResponse.success();
    }

    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> getVideos(Integer size, Integer no, String area) {
        PageResult<Video> result = videoService.pageListVideos(size, no, area);
        return new JsonResponse<>(result);
    }

    @GetMapping("/video-slices")
    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        videoService.viewVideoOnlineBySlices(request, response, url);
    }

    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoLike(videoId, userId);
        return JsonResponse.success();
    }

    @DeleteMapping("/video-likes")
    public JsonResponse<String> deleteVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId, userId);
        return JsonResponse.success();
    }

    @GetMapping("/video-likes")
    public JsonResponse<Map<String, Object>> getVideoLikes(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception ignored) {
        }
        Map<String, Object> result = videoService.getVideoLikes(videoId, userId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/video-collections")
    public JsonResponse<String> addVideoCollection(@RequestBody VideoCollection videoCollection){
        Long currentUserId = userSupport.getCurrentUserId();
        videoService.addVideoCollection(videoCollection, currentUserId);
        return JsonResponse.success();
    }

    @DeleteMapping("/video-collections")
    public JsonResponse<String> deleteVideoCollection(@RequestParam Long videoId){
        Long currentUserId = userSupport.getCurrentUserId();
        videoService.deleteVideoCollection(videoId, currentUserId);
        return JsonResponse.success();
    }

    @GetMapping("/video-collections")
    public JsonResponse<Map<String,Object>> getVideoCollection(@RequestParam Long videoId){
        Long currentUserId = null;
        try{
            currentUserId = userSupport.getCurrentUserId();
        }catch(Exception e){}
        Map<String,Object> result=videoService.getVideoCollection(videoId, currentUserId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/video-coins")
    public JsonResponse<String> addVideoCoins(@RequestBody VideoCoin videoCoin){
        Long currentUserId = userSupport.getCurrentUserId();
        videoService.addVideoCoins(videoCoin, currentUserId);
        return JsonResponse.success();
    }

    @GetMapping("/video-coins")
    public JsonResponse<Map<String,Object>> getVideoCoins(@RequestParam Long videoId){
        Long currentUserId = null;
        try{
            currentUserId = userSupport.getCurrentUserId();
        }catch(Exception e){}
        Map<String,Object> result=videoService.getVideoCoins(videoId, currentUserId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/video-comments")
    public JsonResponse<String> addVideoComment(@RequestBody VideoComment videoComment){
        Long currentUserId = userSupport.getCurrentUserId();
        videoService.addVideoComment(videoComment, currentUserId);
        return JsonResponse.success();
    }

    @GetMapping("/video-comments")
    public JsonResponse<PageResult<VideoComment>> getVideoComment(@RequestParam Integer size,
                                                             @RequestParam Integer no,
                                                             @RequestParam Long videoId) {
        PageResult<VideoComment> result = videoService.getVideoComment(size,no,videoId);
        return new JsonResponse<>(result);
    }

    @GetMapping("/video-detail")
    public JsonResponse<String> getVideoDetail(@RequestParam Long videoId){
        Map<String, Object> result = videoService.getVideoDetail(videoId);
        return JsonResponse.success();
    }
}

