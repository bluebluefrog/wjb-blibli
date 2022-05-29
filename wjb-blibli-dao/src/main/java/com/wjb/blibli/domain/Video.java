package com.wjb.blibli.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

//由于使用es进行查询 es存储的是document类型数据 所以要将数据转换成document类型
//添加注解为类和数据字段进行对应的映射 实现数据转换document类型
//特殊查询字段需要增加不同的注解例如@Id和@Field

//增加@Document说明转换document
//为es提供索引名indexName 为数据进行分类 es通过索引名快速的查找到数据 空则自动创建
@Document(indexName = "videos")
public class Video {

    //增加@Id说明为主键id
    @Id
    private Long id;

    //增加@Field(type = FieldType.Long)说明在es中已Long进行存储
    @Field(type = FieldType.Long)
    private Long userId;//用户id

    private String url; //视频链接

    private String thumbnail;//封面

    //增加@Field(type = FieldType.Text)说明在es中已Text进行存储
    //Text在es中可进行分词查询 es会将输入的关键字进行拆分查询 扩充查询范围
    @Field(type = FieldType.Text)
    private String title; //标题

    private String type;// 0自制 1转载

    private String duration;//时长

    private String area;//分区

    private List<VideoTag> videoTagList;//标签列表

    @Field(type = FieldType.Text)
    private String description;//简介

    //增加@Field(type = FieldType.Date)说明在es中已Date进行存储
    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Date)
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public List<VideoTag> getVideoTagList() {
        return videoTagList;
    }

    public void setVideoTagList(List<VideoTag> videoTagList) {
        this.videoTagList = videoTagList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
