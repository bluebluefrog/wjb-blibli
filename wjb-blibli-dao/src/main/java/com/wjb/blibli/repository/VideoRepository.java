package com.wjb.blibli.repository;

import com.wjb.blibli.domain.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

//es的repo 使用repo对es数据进行增删改查 repo需要继承类ElasticsearchRepository<对象类,主键类型>
//springdata为我们实现了基础的增删改查方法
public interface VideoRepository extends ElasticsearchRepository<Video,Long> {

    //springdata会对方法名进行关键字拆解 然后查询 所以方法命名需要准确
    //find by title like
    Video findByTitleLike(String keword);
}
