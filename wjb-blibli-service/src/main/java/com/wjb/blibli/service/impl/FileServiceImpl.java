package com.wjb.blibli.service.impl;

import com.wjb.blibli.dao.FileDao;
import com.wjb.blibli.domain.File;
import com.wjb.blibli.service.FileService;
import com.wjb.blibli.util.FastDFSUtil;
import com.wjb.blibli.util.MD5Util;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileDao fileDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Override
    public String uploadFileBySlices(MultipartFile slice, String fileMD5, Integer sliceNo, Integer totalSliceNo) throws Exception {

        //根据文件md5获取数据库中file的数据
        File dbFileByMD5 = fileDao.getFileByMD5(fileMD5);
        //如果数据库已存在则直接返回
        if (dbFileByMD5 != null) {
            return dbFileByMD5.getUrl();
        }

        String url = fastDFSUtil.uploadFileBySlices(slice, fileMD5, sliceNo, totalSliceNo);

        //如果数据库不存在 则将url等保存到数据库
        if (!StringUtil.isNullOrEmpty(url)) {
            dbFileByMD5 = new File();
            dbFileByMD5.setCreateTime(new Date());
            dbFileByMD5.setMd5(fileMD5);
            dbFileByMD5.setUrl(url);
            dbFileByMD5.setType(fastDFSUtil.getFileType(slice));
            fileDao.addFile(dbFileByMD5);
        }
        return url;
    }

    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }

}
