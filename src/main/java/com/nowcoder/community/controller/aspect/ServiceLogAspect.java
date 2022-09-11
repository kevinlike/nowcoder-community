package com.nowcoder.community.controller.aspect;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import com.nowcoder.community.util.TimeUtil;

@Component
@Aspect
public class ServiceLogAspect {
    

    private static final Logger logger=LoggerFactory.getLogger(ServiceLogAspect.class);


    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //用户[1.2.3.4],在[time]，访问了[com.nowcoder.community.service.xxx()]

        //获取用户ip地址
        ServletRequestAttributes attributes= (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        //如果是kafka的consumer调用了service则没有attributes，所以需要排除这一情况
        if(attributes==null){
            return;
        }
        HttpServletRequest request=attributes.getRequest();
        String ip=request.getRemoteHost();
        String now=TimeUtil.date2String(new Date());
        String target=joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s]，在[%s]，访问了[%s].", ip,now,target));
    }
}
