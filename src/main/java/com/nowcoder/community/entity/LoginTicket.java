package com.nowcoder.community.entity;

public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private String expired;
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
    public String getTicket() {
        return ticket;
    }
    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getExpired() {
        return expired;
    }
    public void setExpired(String expired) {
        this.expired = expired;
    }
    @Override
    public String toString() {
        return "LoginTicket [expired=" + expired + ", id=" + id + ", status=" + status + ", ticket=" + ticket
                + ", userId=" + userId + "]";
    }
    
    
}
