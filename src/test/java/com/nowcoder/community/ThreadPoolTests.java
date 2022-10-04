package com.nowcoder.community;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.service.AlphaService;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class ThreadPoolTests {
    
    private static final Logger logger=LoggerFactory.getLogger(ThreadPoolTests.class);

    //JDK普通线程池
    private ExecutorService executorService=Executors.newFixedThreadPool(5);//初始化5个线程

    //JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService=Executors.newScheduledThreadPool(5);

    //注入Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    //注入Spring定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AlphaService alphaService;

    //如果测试类的程序结束了，程序中的线程即使没有执行完也会被立即停止，为了避免这样的情况，写一个sleep
    private void sleep(long m){
        try{
            Thread.sleep(m);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    //1.JDK普通线程池
    //@Test
    public void testExecutorService(){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                logger.debug("Hello ExecutorService");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        sleep(10000);
    }

    //2.JDK定时任务线程池
    //@Test
    public void testScheduledExecutorService(){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ScheduledExecutorService");
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 3000, 1000, TimeUnit.MILLISECONDS);
        sleep(15000);
    }

    //3.Spring普通线程池
    //@Test
    public void testThreadPoolTaskExecutor(){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskExecutor");
            }
        };
        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }
        sleep(2000);
    }

    //4.Spring定时任务线程池
    //@Test
    public void testThreadPoolTaskScheduler(){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskScheduler");
            }
        };
        Date startTime=new Date(System.currentTimeMillis()+5000);
        taskScheduler.scheduleAtFixedRate(task, startTime, 1000);
        sleep(15000);
    }

    //5.Spring普通线程池（简化）
    //@Test
    public void testThreadPoolTaskExecutorSimple(){
        for (int i = 0; i < 10; i++) {
            alphaService.execute1();
        }
        sleep(5000);
    }

    //6.Spring定时任务线程池（简化）
    @Test
    public void testThreadPoolTaskSchedulerSimple(){
        //不需要手动调用，会自动执行
        //alphaService.execute2();
        sleep(20000);
    }
}
