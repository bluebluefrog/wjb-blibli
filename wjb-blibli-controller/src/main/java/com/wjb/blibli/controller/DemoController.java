package com.wjb.blibli.controller;

import com.wjb.blibli.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DemoController {

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @GetMapping("/slice")
    public void slices(MultipartFile multipartFile) throws Exception {
        fastDFSUtil.convertFileToSlices(multipartFile);
    }
}
