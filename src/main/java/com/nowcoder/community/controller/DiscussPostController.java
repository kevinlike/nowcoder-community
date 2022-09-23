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
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
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
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

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


        //触发发帖事件，使用kafka异步将发布的帖子加入es中，用于搜索
        Event event=new Event()
            .setTopic(TOPIC_PUBLISH)
            .setUserId(user.getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(post.getId());//因为在discusspost-mapper中insertpost处设置了keyProperty，所以这里可以直接取到id
        eventProducer.fireEvent(event);

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

        //帖子的点赞情况
        long likeCountPost=likeService.findEntityLikeCount(1, post.getId());
        model.addAttribute("likeCountPost", likeCountPost);
        //没登录的话肯定是0，未点赞
        int likeStatusPost=hostHolder.getUser()==null? 0: likeService.findEntityLikeStatus(hostHolder.getUser().getId(), 1, post.getId());
        model.addAttribute("likeStatusPost", likeStatusPost);

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

                //点赞情况
                long likeCountComment=likeService.findEntityLikeCount(2, comment.getId());

                commentVo.put("likeCountComment", likeCountComment);

                int likeStatusComment=hostHolder.getUser()==null?0: likeService.findEntityLikeStatus(hostHolder.getUser().getId(), 2, comment.getId());

                commentVo.put("likeStatusComment", likeStatusComment);

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

                        //回复的点赞情况
                        long likeCountReply=likeService.findEntityLikeCount(2, reply.getId());
                        replyVo.put("likeCountReply",likeCountReply);

                        int likeStatusReply=hostHolder.getUser()==null?0: likeService.findEntityLikeStatus(hostHolder.getUser().getId(), 2, reply.getId());
                        replyVo.put("likeStatusReply", likeStatusReply);

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

    @RequestMapping(path="/my-post/{userId}",method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId")int userId ,Model model,Page page){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user", user);
        int postCount=discussPostService.findDiscussPostRows(userId);
        model.addAttribute("postCount", postCount);
        page.setLimit(5);
        page.setPath("/discuss/my-post/"+userId);
        page.setRows(postCount);

        List<DiscussPost> list= discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> posts=new ArrayList<>();
        if(list!=null){
            for(DiscussPost post:list){
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                posts.add(map);
            }
        }
        model.addAttribute("posts", posts);

        return "/site/my-post";
    }

    //设置置顶
    @RequestMapping(path="/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id, 1);//状态1为置顶

        //触发发帖事件，使用kafka异步将发布的帖子加入es中，用于搜索
        Event event=new Event()
            .setTopic(TOPIC_PUBLISH)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(id);//因为在discusspost-mapper中insertpost处设置了keyProperty，所以这里可以直接取到id
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //设置加精
    @RequestMapping(path="/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id, 1);//状态1为置顶

        //触发发帖事件，使用kafka异步将发布的帖子加入es中，用于搜索
        Event event=new Event()
            .setTopic(TOPIC_PUBLISH)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(id);//因为在discusspost-mapper中insertpost处设置了keyProperty，所以这里可以直接取到id
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //设置删除
    @RequestMapping(path="/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id, 2);//状态1为置顶

        //触发删帖事件，使用kafka异步将发布的帖子加入es中，用于搜索
        Event event=new Event()
            .setTopic(TOPIC_DELETE)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(id);//因为在discusspost-mapper中insertpost处设置了keyProperty，所以这里可以直接取到id
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


}
