package com.nowcoder.community.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;

@Service
public class FollowService implements CommunityConstant{
    

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey=RedisKeyUtil.getFolloweeKey(userId, entityType);
                String follewerKey=RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(follewerKey,userId,System.currentTimeMillis());

                return operations.exec();
            }
        });
    }


    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey=RedisKeyUtil.getFolloweeKey(userId, entityType);
                String follewerKey=RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(follewerKey,userId);

                return operations.exec();
            }
        });
    }

    //查询用户关注的实体数量
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝的数量
    public long findFollowerCount(int entityType,int entityId){
        String follewerKey=RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(follewerKey);
    }

    //查询当前登录的用户是否已关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId)!=null;
    }

    //查询某个用户关注的人
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //redis的range是包含最后一个元素的，所以要“-1”
        //虽然set是无序的但是redis返回的内容是经过排序的，所以还是有序的
        Set<Integer> targetIds= redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset+limit-1);
        if(targetIds==null){
            return null;
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for(Integer targetId:targetIds){
            Map<String,Object> map=new HashMap<>();
            User user=userService.findUserById(targetId);
            map.put("user", user);
            Double score=redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }



    //查询某个用户的粉丝
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey=RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        //redis的range是包含最后一个元素的，所以要“-1”
        Set<Integer> targetIds= redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset+limit-1);
        if(targetIds==null){
            return null;
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for(Integer targetId:targetIds){
            Map<String,Object> map=new HashMap<>();
            User user=userService.findUserById(targetId);
            map.put("user", user);
            Double score=redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
