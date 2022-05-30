package com.wjb.blibli.service.impl;

import com.wjb.blibli.domain.UserInfo;
import com.wjb.blibli.domain.Video;
import com.wjb.blibli.repository.UserInfoRepository;
import com.wjb.blibli.repository.VideoRepository;
import com.wjb.blibli.service.ElasticSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //添加需要查询的投稿视频
    public void addVideo(Video video) {
        //使用es的repo进行添加
        videoRepository.save(video);
    }

    //查询的投稿视频
    public Video getVideos(String keyword) {
        //使用es的repo进行查询 使用keyword
        Video video = videoRepository.findByTitleLike(keyword);

        return video;
    }

    //删除所有视频
    public void deleteVideos(){
        videoRepository.deleteAll();
    }

    //增加UserInfo信息到es
    public void addUserInfo(UserInfo userInfo) {
        userInfoRepository.save(userInfo);
    }

    //该方法查询多个类型条目 视频用户(可增加更多类型)
    public List<Map<String, Object>> getContents(String keyword, Integer pageNo, Integer pageSize) throws IOException {

        String[] indices = {"videos", "user-infos"};
        //构造复杂查询 多种类型查询
        //使用es提供的原生支持SearchRequest 需要传入index es中的索引
        SearchRequest searchRequest = new SearchRequest(indices);
        //和SearchRequest一起使用 配置request
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置searchSourceBuilder 分页数量和大小
        searchSourceBuilder.from(pageNo - 1);
        searchSourceBuilder.size(pageSize);
        //构造多条件查询构造器MultiMatchQueryBuilder
        //传入参数1 keyword 2fieldName需要查询哪些列
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(keyword, "title", "nick", "description");
        //传入多查询配置构造器
        searchSourceBuilder.query(matchQueryBuilder);
        //将配置传给查询请求 实现配置查询功能
        searchRequest.source(searchSourceBuilder);
        //设置超时 当超时终止查询
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //高亮显示
        String[] gaoLiang = {"title", "nick", "description"};
        //es提供用来构建高亮构造器HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //遍历需要高亮的字段
        for (String key : gaoLiang) {
            //.fields()把里面的字段的存储位置取到 .add()添加高亮的地方 参数field
            //new HighlightBuilder.Field(key)构建一个field 参数fieldname传入需要被高亮的filedname
            highlightBuilder.fields().add(new HighlightBuilder.Field(key));
        }
        //如果对多个字段的高亮需要将其设置为false
        highlightBuilder.requireFieldMatch(false);
        //设置高亮样式
        //开始标签
        highlightBuilder.preTags("<span style=\"color:yellow\">");
        //结束标签
        highlightBuilder.postTags("</span>");
        //传入高亮配置构造器
        searchSourceBuilder.highlighter(highlightBuilder);

        //执行搜索查询
        //通过引入restHighLevelClient进行查询参数1SearchRequest 参数2RequestOptions
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //构造返回类型
        List<Map<String, Object>> arrayList = new ArrayList<>();
        //searchResponse.getHits()获取搜索到的条目 类型SearchHit
        for (SearchHit hit : searchResponse.getHits()) {
            //处理高亮字段
            //hit.getHighlightFields()获取高亮的区域
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            //获取查询的内容
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            for (String key : gaoLiang) {
                HighlightField field = highlightFields.get(key);
                if (field != null) {
                    //field.fragments()获取到所有查询出的东西
                    //查询出来可能是多条内容 一句话用逗号分隔可能是两个高亮内容
                    Text[] fragments = field.fragments();
                    //转换成字符串
                    String str = Arrays.toString(fragments);
                    //由于转换的是列表头尾还有括号 所以去除括号
                    str = str.substring(1, str.length() - 1);
                    //将查询出来的内容通过key 替换成str已经专为高亮的字符串
                    sourceMap.put(key, str);
                }
            }
            arrayList.add(sourceMap);
        }
        return arrayList;
    }
}
