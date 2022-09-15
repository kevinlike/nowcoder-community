package com.nowcoder.community.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;

@Component
public class MessageInterceptor implements HandlerInterceptor{

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

                User user=hostHolder.getUser();
                if(user!=null&&modelAndView!=null){
                    int noticeCount=messageService.findNoticeUnreadCount(user.getId(), null)+messageService.findLetterUnreadCount(user.getId(), null);
                    modelAndView.addObject("noticeCount", noticeCount);
                }
                
    }
}
