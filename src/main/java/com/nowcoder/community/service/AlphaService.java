package com.nowcoder.community.service;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.TimeUtil;

@Service
public class AlphaService {

    private static final Logger logger=LoggerFactory.getLogger(AlphaService.class);

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    //用于控制数据库事务的bean，由spring自动创建并且装配
    @Autowired
    private TransactionTemplate transactionTemplate;


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

    /* 
     * 传播机制:A业务可能调取B业务，而两个业务都有各自的隔离机制，两者的协调方案
     * REQUIRED:支持当前事务（即外部事物 eg：A调B 则A是外部事务），若不存在则创建新事务
     * REQUIRED_NEW：创建一个新事务，并暂停当前事务（外部事务）
     * NESTED：若当前存在事务（外部事务），则嵌套在该事务中执行（独立的提交和回滚），否则和REQUIRED一样
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)//该注解用于标注事务，当有报错时整个事务会回滚
    public Object save1(){
        //新增用户
        User user=new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(TimeUtil.date2String(new Date()));
        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道");
        post.setCreateTime(TimeUtil.date2String(new Date()));
        discussPostMapper.insertDiscussPost(post);

        //故意留下的bug，为了测试事务的回滚功能
        Integer.valueOf("abc");

        return "ok";

    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user=new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(TimeUtil.date2String(new Date()));
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post=new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("大家好");
                post.setContent("我是新来的");
                post.setCreateTime(TimeUtil.date2String(new Date()));
                discussPostMapper.insertDiscussPost(post);

                //故意留下的bug，为了测试事务的回滚功能
                Integer.valueOf("abc");

                return null;
            }
        });
    }

    //先在ThreadPoolConfig中配置@EnableAsync，然后在需要的地方注入@Async，就可以实现该方法的多线程运行
    @Async
    public void execute1(){
        logger.debug("execute1");
    }

    //定时任务
    @Scheduled(initialDelay=5000,fixedRate = 1000)
    public void execute2(){
        logger.debug("execute2");
    }
}
