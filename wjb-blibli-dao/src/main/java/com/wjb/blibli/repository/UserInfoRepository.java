package com.wjb.blibli.repository;

import com.wjb.blibli.domain.UserInfo;
import com.wjb.blibli.domain.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserInfoRepository extends ElasticsearchRepository<UserInfo,Long> {

}
