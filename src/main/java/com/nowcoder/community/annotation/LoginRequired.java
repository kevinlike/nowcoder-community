package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//申明注解作用的范围在方法（method）上
@Retention(RetentionPolicy.RUNTIME)//申明注解的作用时间为程序运行时
public @interface LoginRequired {//只要有这个标记就只能在登录时才能访问
    
}
