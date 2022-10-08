package com.nowcoder.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nowcoder.community.entity.DiscussPost;

@Mapper
public interface DiscussPostMapper {

    //重构时加入oederMode，默认为0，当为1时表示希望按照热度排序
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);


    //@Param注解用于给参数取别名，
    //如果只有一个参数，并且在<if>里使用，则必须加别名.
    int selectDiscussPostRows(@Param("userId")int userId);

    int insertDiscussPost(DiscussPost discussPost);

    //根据帖子的id来获取帖子
    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);

    int updateScore(int id,Double score);

}
