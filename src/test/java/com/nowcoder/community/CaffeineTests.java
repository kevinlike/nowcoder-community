package com.nowcoder.community;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.TimeUtil;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class CaffeineTests {
    
    @Autowired
    private DiscussPostService postService;

    //@Test
    public void initDataForTest(){
        for(int i=0;i<300000;i++){
            DiscussPost post=new DiscussPost();
            post.setUserId(111);
            post.setTitle("缓存测试用贴");
            post.setContent("这是一个用于测试本地缓存的压力测试用贴");
            post.setCreateTime(TimeUtil.date2String(new Date()));
            post.setScore(Math.random()*2000);
            postService.addDiscussPost(post);
        }
    }

    @Test
    public void testCache(){
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 0));
    }
}
