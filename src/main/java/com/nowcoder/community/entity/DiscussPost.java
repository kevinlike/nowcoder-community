package com.nowcoder.community.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

//该注解用于es搜索，索引名、分片、备份数量
@Document(indexName="discusspost",shards=6,replicas=3)
public class DiscussPost {

    //这里所有的注解都是用于es标注
    
    @Id//相当于在es中标注了主键
    private int id;

    @Field(type=FieldType.Integer)
    private int userId;

    //搜索的对象
    //假设存储的对象是“互联网校招”，使用ik_max_word就可以拆出尽可能多的词{"互联网","互联","联网","网校"，"校招"}，搜索时使用ik_smart就会拆出较少的词的方式
    @Field(type=FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;
    @Field(type=FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")

    private String content;
    @Field(type=FieldType.Integer)
    private int type;
    @Field(type=FieldType.Integer)
    private int status;
    //@Field(type = FieldType.Text)
    private String createTime;
    @Field(type=FieldType.Integer)
    private int commentCount;
    @Field(type=FieldType.Double)
    private double score;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public int getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "DiscussPost [commentCount=" + commentCount + ", content=" + content + ", createTime=" + createTime
                + ", id=" + id + ", score=" + score + ", status=" + status + ", title=" + title + ", type=" + type
                + ", userId=" + userId + "]";
    }

    
    
}
