package com.nowcoder.community.config;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;

//配置只在第一次的时候被读取到，然后初始化到数据库中，以后quartz直接访问数据库调用，而不是访问配置
//配置->数据库->调用
@Configuration
public class QuartzConfig {
    
    /* 
    FactoryBean可以简化Bean的实例化过程：
    1.通过FactoryBean封装Bean的实例化过程
    2.将FactoryBean装配到Spring容器里
    3.将FactroyBean注入给其他的Bean
    4.该Bean得到的是FactoryBean所管理的对象实例
    */

    //配置JobDetail
    //@Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        //设置名字,不能重复
        factoryBean.setName("alphaJob");
        //设置小组,多个任务可以属于一个组
        factoryBean.setGroup("alphaJobGroup");
        //任务是否长久保存，哪怕以后trigger都没了，任务还保存着
        factoryBean.setDurability(true);
        //设置任务是否可恢复，如果以后任务有问题，可以恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    /* 
     * 配置Trigger有两种方法
     * 1.这里用的是SimpleTriggerFactoryBean，这是简化的方法
     * 2.CronTriggerFactoryBean，可以实现一些复杂的功能，例如每月底执行某功能，或者每周一执行某功能
     */
    //@Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean=new SimpleTriggerFactoryBean();
        //trigger要执行的任务
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        //设置任务的重复时间间隔ms
        factoryBean.setRepeatInterval(3000);
        //trigger需要存储任务的状态，这里指出其存储的对象,这里用的是默认类型
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    //刷新帖子分数
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        //设置名字,不能重复
        factoryBean.setName("postScoreRefreshJob");
        //设置小组,多个任务可以属于一个组
        factoryBean.setGroup("communityJobGroup");
        //任务是否长久保存，哪怕以后trigger都没了，任务还保存着
        factoryBean.setDurability(true);
        //设置任务是否可恢复，如果以后任务有问题，可以恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshJobTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean=new SimpleTriggerFactoryBean();
        //trigger要执行的任务
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        //设置任务的重复时间间隔ms
        factoryBean.setRepeatInterval(1000*60*5);
        //trigger需要存储任务的状态，这里指出其存储的对象,这里用的是默认类型
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
    
}
