package com.nowcoder.community.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.TimeUtil;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant{

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path="/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId,Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(TimeUtil.date2String(new Date()));
        commentService.addComment(comment);

        return "redirect:/discuss/detail/"+discussPostId;
    }

    @RequestMapping(path="/my-reply/{userId}",method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId,Model model,Page page){
        User user =userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        int replyCount=commentService.findCommentCountByUser(ENTITY_TYPE_POST, userId);
        model.addAttribute("replyCount", replyCount);

        page.setLimit(5);
        page.setPath("/comment/my-reply/"+userId);
        page.setRows(replyCount);
        List<Comment> list= commentService.findCommentsByUserId(ENTITY_TYPE_POST, userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> comments=new ArrayList<>();
        if(list!=null){
            for(Comment comment:list){
                Map<String,Object> map=new HashMap<>();
                map.put("comment", comment);
                DiscussPost post= discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("post", post);
                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                map.put("likeCount", likeCount);
                comments.add(map);
            }
        }
        model.addAttribute("comments", comments);
        return "/site/my-reply";
    }

}
