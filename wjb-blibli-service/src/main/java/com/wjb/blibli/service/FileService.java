package com.wjb.blibli.service;

import com.wjb.blibli.domain.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadFileBySlices(MultipartFile slice,
                              String fileMD5,
                              Integer sliceNo,
                              Integer totalSliceNo) throws Exception;

    String getFileMD5(MultipartFile file) throws Exception;

    File getFileByMd5(String fileMd5);
}
