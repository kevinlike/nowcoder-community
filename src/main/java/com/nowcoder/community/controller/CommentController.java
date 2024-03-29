package com.nowcoder.community.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
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

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    //添加评论
    @RequestMapping(path="/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId,Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(TimeUtil.date2String(new Date()));
        commentService.addComment(comment);

        //触发评论事件,调用生产者将数据加入kafka
        Event event=new Event()
            .setTopic(TOPIC_COMMENT)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(comment.getEntityType())
            .setEntityId(comment.getEntityId())
            .setData("postId", discussPostId);
        //如果评论对象是一个帖子，则通知帖子的发布者
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost target=discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }//若评论的对象是一个回复，则通知回复的发布者
        else if(comment.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment target=commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);
        //如果是对帖子的评论，则需要加入
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //触发发帖事件，使用kafka异步将发布的帖子加入es中，用于搜索
            event=new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(comment.getUserId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);//因为在discusspost-mapper中insertpost处设置了keyProperty，所以这里可以直接取到id
            eventProducer.fireEvent(event);

            //重新计算帖子的热度得分
            String redisKey=RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }
        
        
        return "redirect:/discuss/detail/"+discussPostId;
    }

    //展示某用户的回帖列表
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
