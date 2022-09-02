package com.nowcoder.community.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;

@Controller
public class LikeController {
    
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path="/like",method=RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId){
        User user=hostHolder.getUser();
        //点赞
        likeService.like(user.getId(), entityType, entityId,entityUserId);
        //获取总点赞数
        long likeCount=likeService.findEntityLikeCount(entityType, entityId);
        //获取当前用户是否点赞的状态
        int likeStatus=likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //返回给前端的结果
        Map<String,Object> map=new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return CommunityUtil.getJSONString(0, null,map);
    }
}