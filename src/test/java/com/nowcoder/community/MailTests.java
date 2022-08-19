package com.nowcoder.community;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.MailClient;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;//模版引擎，用于定位邮件模版位置

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    //@Test
    public void testTextMail(){
        mailClient.sendMail("kevinkinglike@gmail.com", "test", "testTextMail");
    }

    //@Test
    public void testHtmlMail(){
        Context context=new Context();
        context.setVariable("username", "Chelsey");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("kevinkinglike@gmail.com", "HTML", content);
    }

    @Test
    public void testVerificationCode(){
        String email="997933785@qq.com";
        //Map<String,Object> map =userService.getVerificationCode(email);
        //System.out.println(map.toString());
        //User user=userMapper.selectByEmail(email);
        //int id=user.getId();
        //userMapper.updateActivationCode(id, "abcd");
        //String activationCode=user.getActivationCode();
        //System.out.println(activationCode);
    }
}
