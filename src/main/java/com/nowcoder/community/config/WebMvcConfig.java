package com.nowcoder.community.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nowcoder.community.controller.interceptor.AlphaInterceptor;
import com.nowcoder.community.controller.interceptor.LoginRequiredInterceptor;
import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import com.nowcoder.community.controller.interceptor.MessageInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
    
    @Autowired
    private AlphaInterceptor alphaInterceptor;
    
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    //一开始的登录验证是用拦截器实现的，后来改用security，所以这个方法废弃了
    // @Autowired
    // private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //设置拦截器的拦截范围，和不拦截的目标
        registry.addInterceptor(alphaInterceptor)
        .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
        .addPathPatterns("/register","/login");
        // TODO Auto-generated method stub
        //WebMvcConfigurer.super.addInterceptors(registry);

        registry.addInterceptor(loginTicketInterceptor)
        .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        //登录认证由拦截器改为由security实现，此处废弃
        // registry.addInterceptor(loginRequiredInterceptor)
        // .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
        .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        
    }
}
