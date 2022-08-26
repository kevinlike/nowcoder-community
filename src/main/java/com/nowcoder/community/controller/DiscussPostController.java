package com.nowcoder.community.controller;

import java.util.Date;

import org.apache.catalina.Host;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.TimeUtil;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path="/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user=hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403, "当前还未登录");
        }

        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(TimeUtil.date2String(new Date()));
        discussPostService.addDiscussPost(post);

        //报错未来统一处理
        return CommunityUtil.getJSONString(0, "发布成功");

    }

    @RequestMapping(path="/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,Model model){
        //帖子
        DiscussPost post=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //查询帖子作者
        User user=userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        return "/site/discuss-detail";
    }
}
