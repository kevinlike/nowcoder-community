package com.nowcoder.community;

import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    //@Test
    public void testSelectUser(){
        User user=userMapper.selectById(101);
        System.out.println("------------------------");
        System.out.println(user);
        user=userMapper.selectByName("guanyu");
        System.out.println(user);
        user=userMapper.selectByEmail("nowcoder103@sina.com");
        System.out.println(user);
    }

    //@Test
    public void testInsertUser(){
        User user=new User();
        user.setUsername("test");
        user.setPassword("12345");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月和小时的格式为两个大写字母
        java.util.Date date = new Date();//获得当前时间
        String birthday = df.format(date);//将当前时间转换成特定格式的时间字符串，这样便可以插入到数据库中
        System.out.println(birthday);

        user.setCreateTime(birthday); 
        
        int rows=userMapper.insertUser(user);
        System.out.println(rows);
    }

    //@Test
    public void updateUser(){
        
        int rows=userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows=userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows=userMapper.updatePassword(150, "123456789");
        System.out.println(rows);


    }

    //@Test
    public void testSelectPosts(){
        List<DiscussPost> list= discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost post:list)
        {
            System.out.println(post);
        }
        
        int rows=discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    //@Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired("2022/8/17 5:22:18");
        loginTicketMapper.insertLoginTicket(loginTicket);

    }

    //@Test
    public void testSelectLoginTicket(){
        System.out.println("-------------------==================++++++++++++++++++++++++++");
        LoginTicket loginTicket=loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc", 1);
        loginTicket=loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        

    }

}
