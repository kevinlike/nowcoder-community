package com.nowcoder.community.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.nowcoder.community.util.RedisKeyUtil;

@Service
public class LikeService {
    
    @Autowired
    private RedisTemplate redisTemplate;

    //点赞 userId是点赞者的id，entityUserId是被点赞者的id
    public void like(int userId,int entityType,int entityId,int entityUserId){
        // String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        // if(isMember){
        //     redisTemplate.opsForSet().remove(entityLikeKey, userId);
        // }
        // else{
        //     redisTemplate.opsForSet().add(entityLikeKey, userId);
        // }
        //点赞需要对数据库进行两步操作，所以要建立事务处理
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey=RedisKeyUtil.getUserLikeKey(entityUserId);

                boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                //先进行上面的查询，再开启事务，因为在事务的过程中不能查询
                operations.multi();

                if(isMember){
                    //取消赞
                    operations.opsForSet().remove(entityLikeKey,userId);
                    //用户的被赞次数-1
                    operations.opsForValue().decrement(userLikeKey);
                }
                else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    //查询某实体点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean like=redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(like){
            return 1;
        }
        else{
            return 0;
        }
    }

    //查询某个用户获得赞的数量
    public int findUserLikeCount(int userId){
        String userLikeKey=RedisKeyUtil.getUserLikeKey(userId);

        Integer count=(Integer)redisTemplate.opsForValue().get(userLikeKey);

        return count==null?0:count;
    }
}
