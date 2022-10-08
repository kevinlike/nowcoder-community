package com.nowcoder.community.quartz;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;

public class PostScoreRefreshJob implements Job,CommunityConstant{

    private static final Logger logger=LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    //牛客纪元
    private static final Date epoch;

    static{
        try {
            epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("初始化牛客纪元失败！",e);
        }
    }

    @Autowired
    private ElasticsearchService elasticsearchService;

    public Date string2Date(String time){
        try {
			Date createDate=DateUtils.parseDate(time, "yyyy-MM-dd HH:mm:ss");
            return createDate;
			//System.out.println(createDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            return new Date();
		}
    }
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        // TODO Auto-generated method stub
        String redisKey=RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations=redisTemplate.boundSetOps(redisKey);
        if(operations.size()==0){
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数："+operations.size());
        while(operations.size()>0){
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕！");
        
    }
    private void refresh(int postId){
        DiscussPost post=discussPostService.findDiscussPostById(postId);
        if(post==null){
            logger.error("该帖子不存在：ID="+postId);
            return;
        }
        //是否精华
        boolean wonderful=post.getStatus()==1;
        //评论数量
        int commentCount=post.getCommentCount();
        //点赞数量
        long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        //计算权重
        double w=(wonderful?75:0)+commentCount*10+likeCount*2;
        //将string日期转为Date
        Date createDate=string2Date(post.getCreateTime());
        //createDate=DateUtils.parseDate(post.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
        //分数=帖子权重+距离天数
        double score=Math.log10(Math.max(w,1.0))+(createDate.getTime()-epoch.getTime())/(1000*3600*24);
        //更新帖子分数
        discussPostService.updateScore(postId, score);
        //同步es的数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
