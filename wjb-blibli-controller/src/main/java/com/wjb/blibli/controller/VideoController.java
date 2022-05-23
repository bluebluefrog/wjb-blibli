package com.wjb.blibli.controller;

import com.wjb.blibli.controller.support.UserSupport;
import com.wjb.blibli.domain.JsonResponse;
import com.wjb.blibli.domain.PageResult;
import com.wjb.blibli.domain.Video;
import com.wjb.blibli.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}
