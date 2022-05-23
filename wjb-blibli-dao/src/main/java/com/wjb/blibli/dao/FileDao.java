package com.wjb.blibli.dao;

import com.wjb.blibli.domain.File;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface FileDao {

    Integer addFile(File file);

    File getFileByMD5(String md5);
}
