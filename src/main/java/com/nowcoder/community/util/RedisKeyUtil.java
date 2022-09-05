package com.nowcoder.community.util;



//简单的小工具，不需要添加bean，直接用静态方法访问即可
public class RedisKeyUtil {
    
    private static final String SPLIT=":";
    private static final String PREFIX_ENTITY_LIKE="like:entity";
    private static final String PREFIX_USER_LIKE="like:user";
    private static final String PREFIX_FOLL0WEE="followee";
    private static final String PREFIX_FOLL0WER="follower";

    //某个实体的赞
    //like:entity:entityType:entityId ->set(userId)
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    //获取某个用户的赞
    //like:user:userId->int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType->zset(entityId,now) now是当前时间的整数
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLL0WEE+SPLIT+userId+SPLIT+entityType;
    }

    //某个用户拥有的粉丝
    //follower:entityType:entityId->zset(userId,now) now是当前时间的整数
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLL0WER+SPLIT+entityType+SPLIT+entityId;
    }

}
