package com.nowcoder.community.util;



//简单的小工具，不需要添加bean，直接用静态方法访问即可
public class RedisKeyUtil {
    
    private static final String SPLIT=":";
    private static final String PREFIX_ENTITY_LIKE="like:entity";
    private static final String PREFIX_USER_LIKE="like:user";
    private static final String PREFIX_FOLL0WEE="followee";
    private static final String PREFIX_FOLL0WER="follower";
    private static final String PREFIX_KAPTCHA="kaptcha";
    private static final String PREFIX_TICKET="ticket";
    private static final String PREFIX_USER="user";
    private static final String PREFIX_UV="uv";
    private static final String PREFIX_DAU="dau";

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

    //登录验证码的key
    //用户访问登录页面时，给他发一个随机字符串，存入cookie，用以标识该用户
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }

    //用户
    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }

    //unique visitor独立访客，通过ip地址排重统计数据，每次访问都统计，没登录的用户也会被统计
    //单日UV
    public static String getUVKey(String date){
        return PREFIX_UV+SPLIT+date;
    }

    //区间UV
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV+SPLIT+startDate+SPLIT+endDate;
    }

    //daily active user日活跃用户，通过用户id排重统计
    //单日DAU
    public static String getDAUKey(String date){
        return PREFIX_DAU+SPLIT+date;
    }

    //区间DAU
    public static String gatDAUKey(String startDate,String endDate){
        return PREFIX_DAU+SPLIT+startDate+SPLIT+endDate;
    }

}
