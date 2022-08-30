package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.service.AlphaService;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class TransactionTests {
    @Autowired
    private AlphaService alphaService;


    //里面故意留了bug
    //@Test
    public void testSave1(){
        Object obj=alphaService.save1();
        System.out.println(obj);
    }

    //里面故意留了bug
    //@Test
    public void testSave2(){
        Object obj=alphaService.save1();
        System.out.println(obj);
    }
}
