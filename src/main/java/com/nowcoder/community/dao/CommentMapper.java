package com.nowcoder.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.nowcoder.community.entity.Comment;

@Mapper
public interface CommentMapper {
    //entityId是某个帖子或者评论，此函数的作用是找到对某个帖子或者评论的所有评论
    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);
}
