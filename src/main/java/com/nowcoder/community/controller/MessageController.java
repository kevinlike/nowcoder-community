package com.nowcoder.community.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.TimeUtil;

@Controller
public class MessageController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;
    //私信列表请求
    @RequestMapping(path="/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model,Page page){

        User user =hostHolder.getUser();

        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setLimit(5);
        //会话列表
        List<Message> conversationList=messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations=new ArrayList<>();
        if(conversationList!=null){
            for(Message message:conversationList){
                Map<String,Object> map=new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId=user.getId()==message.getFromId()?message.getToId():message.getFromId();
                User fromUser=userService.findUserById(targetId);
                map.put("target", fromUser);

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查询未读消息数量
        int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
        
    }

    //与某用户的对话框
    @RequestMapping(path="/letter/detail/{conversationId}",method=RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList=messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters=new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                Map<String,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        //查询私信目标
        model.addAttribute("target", getLetterTarget(conversationId));
        //设置已读
        List<Integer> ids=getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";


    }

    private User getLetterTarget(String conversationId){
        String[] ids=conversationId.split("_");
        int id0=Integer.parseInt(ids[0]);
        int id1=Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId()==id0){
            return userService.findUserById(id1);
        }
        else{
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids=new ArrayList<>();

        if(letterList!=null){
            for(Message message:letterList){
                if(hostHolder.getUser().getId()==message.getToId()&& message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(path="/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        Integer.valueOf("abc");
        User fromUser=hostHolder.getUser();
        User toUser=userService.findUserByName(toName);
        if(toUser==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message=new Message();
        message.setContent(content);
        message.setFromId(fromUser.getId());
        message.setToId(toUser.getId());
        message.setStatus(0);
        message.setCreateTime(TimeUtil.date2String(new Date()));
        String conversationId="";
        if(fromUser.getId()<toUser.getId()){
            conversationId=Integer.toString(fromUser.getId())+"_"+Integer.toString(toUser.getId());
        }
        else{
            conversationId=Integer.toString(toUser.getId())+"_"+Integer.toString(fromUser.getId());
        }
        message.setConversationId(conversationId);
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path="/letter/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(Integer messageId){
        List<Integer> ids=new LinkedList<>();
        ids.add(messageId);
        int row=messageService.deleteMessage(ids);
        if(row==0){
            return CommunityUtil.getJSONString(1, "没有删除任何信息");
        }
        return CommunityUtil.getJSONString(0);
    }

}
