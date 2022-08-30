package com.nowcoder.community.entity;


public class Comment {
    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private String createTime;
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
    public int getEntityType() {
        return entityType;
    }
    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }
    public int getEntityId() {
        return entityId;
    }
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
    public int getTargetId() {
        return targetId;
    }
    public void setTargetId(int targetId) {
        this.targetId = targetId;
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
        return "Comment [content=" + content + ", createTime=" + createTime + ", entityId=" + entityId + ", entityType="
                + entityType + ", id=" + id + ", status=" + status + ", targetId=" + targetId + ", userId=" + userId
                + "]";
    }
    
}
