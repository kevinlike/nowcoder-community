package com.nowcoder.community.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;

@Controller
public class FollowController implements CommunityConstant{
    
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired//自定义的注解，表示只有在登录时才能访问
    @RequestMapping(path="/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user= hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        //触发关注事件
        Event event=new Event()
            .setTopic(TOPIC_FOLLOW)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(entityType)
            .setEntityId(entityId)
            .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注");
    }

    @LoginRequired//自定义的注解，表示只有在登录时才能访问
    @RequestMapping(path="/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user= hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    @RequestMapping(path="/followees/{userId}",method=RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId,Page page,Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setPath("/followees/"+userId);
        page.setLimit(5);
        page.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String,Object>> userList= followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(userList!=null){
            for(Map<String,Object> map:userList){
                User followee=(User) map.get("user");
                boolean hasFollowed=hasFollowed(followee.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    @RequestMapping(path="/followers/{userId}",method=RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId,Page page,Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setPath("/followers/"+userId);
        page.setLimit(5);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));


        List<Map<String,Object>> userList= followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(userList!=null){
            for(Map<String,Object> map:userList){
                User follower=(User) map.get("user");
                boolean hasFollowed=hasFollowed(follower.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    private boolean hasFollowed(int userId){
        if(hostHolder.getUser()==null){
            return false;
        }
        else{
            return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
    }
}