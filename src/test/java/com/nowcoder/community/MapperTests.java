package com.nowcoder.community;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectUser(){
        User user=userMapper.selectById(101);
        System.out.println("------------------------");
        System.out.println(user);
        user=userMapper.selectByName("guanyu");
        System.out.println(user);
        user=userMapper.selectByEmail("nowcoder103@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user=new User();
        user.setUsername("test");
        user.setPassword("12345");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        //user.setCreateTime(new java.sql.Date(0)); 
        
        //int rows=userMapper.insertUser(user);
        //System.out.println(rows);
    }

    @Test
    public void updateUser(){
        
        int rows=userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows=userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows=userMapper.updatePassword(150, "123456789");
        System.out.println(rows);


    }

}
