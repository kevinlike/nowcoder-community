package com.nowcoder.community.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;

import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;

import co.elastic.clients.elasticsearch.xpack.usage.Security;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor{
    
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        //从cookie中获取登录凭证号码
        String ticket=CookieUtil.getValue(request, "ticket");

        if(ticket!=null){
            //获取凭证的所有信息
            LoginTicket loginTicket=userService.findLoginTicket(ticket);

            //判断凭证是否还有效
            if(loginTicket!=null&&loginTicket.getStatus()==0){
                //根据凭证查询用户
                User user=userService.findUserById(loginTicket.getUserId());

                /*在本次请求中持有用户
                **服务器在处理浏览器的请求时，可能同时会有多个线程处理多个浏览器的请求
                **如果不进行线程隔离可能会造成访问冲突，所以需要把各线程的数据存到各线程内部
                */
                hostHolder.setUser(user);
                //构建用户认证的结果，并存入SecurityContext，以便Security进行授权
                Authentication authentication=new UsernamePasswordAuthenticationToken(user, user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }

        }
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        
        User user = hostHolder.getUser();
        if(user!=null&&modelAndView!=null)
        {
            modelAndView.addObject("loginUser", user);
            //System.out.println("1111111111111111");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        hostHolder.clear();
        //SecurityContextHolder.clearContext();
    }
}
