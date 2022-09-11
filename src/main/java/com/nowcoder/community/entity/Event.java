package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    
    private String topic;
    //事件触发人
    private int userId;
    //触发的实体类型，id和作者
    private int entityType;
    private int entityId;
    private int entityUserId;
    //将来可能还有别的数据需要处理，所以提供一个map，作为对于未来需求的扩展
    private Map<String,Object> data=new HashMap<>();

    /* 
     * 这里的set方法进行了改变，以往都是void，但是现在改成有返回event的
     * 好处是可以在set操作时进行set(a).set(b),连续操作
     */
    public String getTopic() {
        return topic;
    }
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }
    public int getUserId() {
        return userId;
    }
    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }
    public int getEntityType() {
        return entityType;
    }
    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }
    public int getEntityId() {
        return entityId;
    }
    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }
    public int getEntityUserId() {
        return entityUserId;
    }
    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }
    public Map<String, Object> getData() {
        return data;
    }
    // public Event setData(Map<String, Object> data) {
    //     this.data = data;
    //     return this
    // }
    public Event setData(String key,Object value) {
        this.data.put(key, value);
        return this;
    }

    
}
