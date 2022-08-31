package com.nowcoder.community.entity;

public class Message {
     private int id;
     private int fromId;
     private int toId;
     private String conversationId;
     private String content;
     private int status;
     private String createTime;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getFromId() {
        return fromId;
    }
    public void setFromId(int fromId) {
        this.fromId = fromId;
    }
    public int getToId() {
        return toId;
    }
    public void setToId(int toId) {
        this.toId = toId;
    }
    public String getConversationId() {
        return conversationId;
    }
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
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
    @Override
    public String toString() {
        return "Message [content=" + content + ", conversationId=" + conversationId + ", createTime=" + createTime
                + ", fromId=" + fromId + ", id=" + id + ", status=" + status + ", toId=" + toId + "]";
    }
     

}
