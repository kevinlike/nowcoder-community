package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.util.SensitiveFilter;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class SensitiveFilterTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;


    @Test
    public void testSensitiveFilter(){
        String text="这里可以赌博，可以嫖娼，可以开票，哈哈哈！";
        text=sensitiveFilter.filter(text);
        System.out.print(text);
    }

}

