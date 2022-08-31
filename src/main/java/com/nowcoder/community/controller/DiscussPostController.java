package com.nowcoder.community.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Host;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.TimeUtil;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant{
    
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

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
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,Model model,Page page){
        //帖子
        DiscussPost post=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //查询帖子作者
        User user=userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给回复的评论
        //commentList里面存放针对帖子post的所有评论
        List<Comment> commentList=commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //commentVoList存放评论的具体信息，发表者的用户信息，以及对该评论的回复
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                //评论Vo
                Map<String,Object> commentVo=new HashMap<>();
                //评论
                commentVo.put("comment", comment);
                //评论的作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //回复列表
                List<Comment> replyList= commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复Vo列表
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if(replyList !=null){
                    for(Comment reply:replyList){
                        Map<String,Object> replyVo=new HashMap<>();
                        //回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target=reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复数量
                int replyCount=commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);


            }
        }
        model.addAttribute("comments", commentVoList);
        
        return "/site/discuss-detail";
    }


}