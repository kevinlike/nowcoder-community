package com.nowcoder.community.controller.advice;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nowcoder.community.util.CommunityUtil;

//如果不加annotations会扫描所有bean，但是我们只需扫描带controller的bean就行，所以加上一个annotations
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger=LoggerFactory.getLogger(ExceptionAdvice.class);

    //{}里面放的是需要处理的异常种类，Exception.class是所有异常的父类，相当于所有异常都由下函数处理
    @ExceptionHandler({Exception.class})
    /* 
     * 参数：Exception e是controller中发生的异常
     *      HttpServletRequest request用户的请求
     *      HttpServletResponse response服务器的响应
     */ 
    public void handleException(Exception e,HttpServletRequest request,HttpServletResponse response) throws IOException{
        //将异常记入日志,e,getMessage()是对异常的一个概括
        logger.error("服务器发生异常："+e.getMessage());
        //异常的详细内容 每个element记录了一条异常信息
        for(StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }
        /* 
         * 如果是对页面的请求则跳转到错误提示页面，如果是异步请求则返回json数据
         * 所以需要先对请求的类型进行一个判断
         */
        String xRequestedWith=request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            //这里可以用application/json，浏览器会直接接收到json数据，如果用plain，需要我们手动使用$.parseJSON(data)将数据转为json格式
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer=response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "来自ExceptionAdvice:服务器发生异常！"));
        }
        else{
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}
