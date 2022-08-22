package com.nowcoder.community.util;

import org.springframework.stereotype.Component;

import com.nowcoder.community.entity.User;

//用于持有用户信息，代替session对象
@Component
public class HostHolder {
    
    //threadlocal方法是以线程为key进行存储值的，不同的线程相互独立
    private ThreadLocal<User> users=new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
    }
}
