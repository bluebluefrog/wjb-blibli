package com.wjb.blibli.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileService {

    String uploadFileBySlices(MultipartFile slice,
                              String fileMD5,
                              Integer sliceNo,
                              Integer totalSliceNo) throws Exception;

    String getFileMD5(MultipartFile file) throws Exception;

}
