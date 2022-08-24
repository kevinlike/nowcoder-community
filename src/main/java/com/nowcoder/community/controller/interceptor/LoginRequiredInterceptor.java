package com.nowcoder.community.controller.interceptor;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;



//有些功能需要登录后才能查看，所以必须在这些网页打开前检查用户是否已经登录
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor{
    
    @Autowired
    private HostHolder hostHolder;//获取当前用户

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)//handler是被拦截的目标
            throws Exception {
        //判断被拦截的目标是不是方法，不是方法不处理（可能是静态资源等），是方法才处理，如果是方法则一定是HandlerMethod类型
        if(handler instanceof HandlerMethod){
            //对目标进行转型，从而方便调用其方法
            HandlerMethod handlerMethod=(HandlerMethod) handler;
            //获取其调用的method对象
            Method method=handlerMethod.getMethod();
            //从方法对象尝试去取其注解,getAnnotation(xxx.class)用于去取注解中符合xxx类型的注解
            LoginRequired loginRequired= method.getAnnotation(LoginRequired.class);
            if(loginRequired!=null&&hostHolder.getUser()==null)
            {
                //拒绝请求
                //重定向
                response.sendRedirect(request.getContextPath() +"/login");
                System.out.println(request.getContextPath());
                return false;
            }

        }
        return true;
    }
}
