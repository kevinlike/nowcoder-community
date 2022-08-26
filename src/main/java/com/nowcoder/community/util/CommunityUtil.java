package com.nowcoder.community.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSONObject;




public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    } 

    //MD5加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    } 
    
/* 
 * 
 */
    public static String getJSONString(int code,String msg,Map<String,Object> map){
        JSONObject json=new JSONObject();
        json.put("code", code);
        json.put("msg",msg);
        if(map!=null&&!map.isEmpty()){
            for(String key:map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code,String msg){
        
        return getJSONString(code, msg,null);
    }

    public static String getJSONString(int code){

        return getJSONString(code, null,null);
        
    }

    public static void main(String[] arg){
        Map<String,Object> map=new HashMap<>();
        map.put("name","alice");
        map.put("age",25);
        System.out.println(getJSONString(0, "ok", map));
    }
    
}
