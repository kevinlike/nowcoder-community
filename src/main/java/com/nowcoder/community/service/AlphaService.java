package com.nowcoder.community.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nowcoder.community.dao.AlphaDao;

@Service
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;


    public AlphaService(){
        System.out.println("实例化AlphaService");
    }
    @PostConstruct//方法会在构造器之后调用
    public void init(){
        System.out.println("初始化AlphaServcice");
    }

    @PreDestroy//方法会在销毁对象之前调用
    public void destroy() {
        System.out.println("销毁AlphaService");
    }

    public String find(){
        return alphaDao.select();
    }
}
