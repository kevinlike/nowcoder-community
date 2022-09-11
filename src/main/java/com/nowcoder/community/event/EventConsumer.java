package com.nowcoder.community.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.TimeUtil;

@Component
public class EventConsumer implements CommunityConstant{
    
    private static final Logger logger=LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics={TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空！");
            return;
        }
        //将生产者生产的消息解析为json格式，需要注明数据类型（Event.class）
        Event event=JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误！");
            return;
        }

        //发送站内通知
        Message message=new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(TimeUtil.date2String(new Date()));

        Map<String,Object> content=new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("eventId",event.getEntityId());

        if(!event.getData().isEmpty()){
            //下列方法为遍历一个map的方法
            //event.getData是Event中存储数据的部分
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }
        //将数据以json字符串的形式存入content
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

}
