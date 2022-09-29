package com.nowcoder.community.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DataService;
import com.nowcoder.community.util.HostHolder;



//用于统计UV和DAU
@Component
public class DataInterceptor implements HandlerInterceptor{
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // TODO Auto-generated method stub
        //统计UV
        String ip=request.getRemoteHost();
        dataService.recordUV(ip);
        //统计DAU
        User user=hostHolder.getUser();
        if(user!=null){
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
