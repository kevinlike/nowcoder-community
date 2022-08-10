package com.nowcoder.community.entity;



public class User {
    private int  id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private String createTime;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getSalt() {
        return salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
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
    public String getActivationCode() {
        return activationCode;
    }
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
    public String getHeaderUrl() {
        return headerUrl;
    }
    public void setHeaderUrl(String headerUrl) {
        this.headerUrl = headerUrl;
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String birthday) {
        this.createTime = birthday;
    }
    @Override
    public String toString() {
        return "User [activationCode=" + activationCode + ", createTime=" + createTime + ", email=" + email
                + ", headerUrl=" + headerUrl + ", id=" + id + ", password=" + password + ", salt=" + salt + ", status="
                + status + ", type=" + type + ", username=" + username + "]";
    }

    
}
