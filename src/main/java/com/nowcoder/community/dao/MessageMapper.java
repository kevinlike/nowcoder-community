package com.nowcoder.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.nowcoder.community.entity.Message;

@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表，针对每个会话只返回一条最新的信息
    List<Message> selectConversations(int userId,int offset,int limit);
    
    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    int selectLetterUnreadCount(int userId,String conversationId);

    //新增消息
    int insertMessage(Message message);

    //修改消息状态，未读->已读，xx->删除
    int updateStatus(List<Integer> ids,int status);

}
