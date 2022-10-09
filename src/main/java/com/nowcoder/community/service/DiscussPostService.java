package com.nowcoder.community.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;

@Service
public class DiscussPostService {

    private static final Logger logger=LoggerFactory.getLogger(DiscussPostService.class);
    
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //caffeine核心接口：Cache，LoadingCache,AsyncLoadingCache

    //帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<String,List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception{
                        if(key==null||key.length()==0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        //key的格式应该是offset:limit 判断其合法性
                        String[] params=key.split(":");
                        if(params==null||params.length!=2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset=Integer.valueOf(params[0]);
                        int limit=Integer.valueOf(params[1]);
                        //这里还可以添加一个二级缓存redis，如果没有再访问数据库
                        
                        logger.debug("load post list from DB");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        //初始化帖子总数缓存
        postRowsCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer,Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception{
                        logger.debug("load post rows from DB");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }


    //用户id为0，则不考虑用户id，若用户id！=0则选取对应用户发布的帖子
    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        //很多方法都会调用这个方法，但是只有搜索热门帖子时才需要缓存数据，而热门帖子的参数userid=0，orderMode=1.
        //offset和limit可以唯一标志出一页帖子列表，所以将这两者的结合作为key
        if(userId==0&&orderMode==1){
            return postListCache.get(offset+":"+limit);
        }
        //访问数据库时记个日志
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    } 

    public int findDiscussPostRows(int userId){
        //记录总的帖子页数，即userid=0，其他情况不用缓存
        if(userId==0){
            //由于必须传一个key，所以就用userid，其实结果和userid没关系
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if(post==null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //转义HTML标记,防止<script></script>这样的标签对网页造成损害
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);

    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id,int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id,Double score){
        return discussPostMapper.updateScore(id, score);
    }
}
