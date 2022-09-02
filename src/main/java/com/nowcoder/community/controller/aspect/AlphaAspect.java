package com.nowcoder.community.controller.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    
    /* 
     * execution是一个固定的关键字
     * 第一个*表示方法的返回值，任何返回值都行
     * com.nowcoder.community.service是包名，之后的*表示所有类
     * 之后的*表示所有方法
     * (..)表示所有的参数
     * 以上表示所有service下所有类，所有方法，所有参数，所有返回值都要处理
     */
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("around Before");
        Object obj= joinPoint.proceed();
        System.out.println("around After");
        return obj;
    }
}
