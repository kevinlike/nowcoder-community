package com.nowcoder.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;

@Service
public class UserService {
    
    @Autowired
    private UserMapper  userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public List<User> findAllUsers(){
        return userMapper.selectAllUsers();
    }
}
