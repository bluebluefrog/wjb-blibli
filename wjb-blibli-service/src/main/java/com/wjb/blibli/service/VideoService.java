package com.wjb.blibli.service;

import com.wjb.blibli.domain.PageResult;
import com.wjb.blibli.domain.Video;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface VideoService {
    void addVideo(Video video);

    PageResult<Video> pageListVideos(Integer size, Integer no, String area);

    void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception;
}
