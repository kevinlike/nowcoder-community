package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nowcoder.community.util.MailClient;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    //@Test
    public void testTextMail(){
        mailClient.sendMail("kevinkinglike@gmail.com", "test", "testTextMail");
    }

    @Test
    public void testHtmlMail(){
        Context context=new Context();
        context.setVariable("username", "Chelsey");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("kevinkinglike@gmail.com", "HTML", content);
    }
}
