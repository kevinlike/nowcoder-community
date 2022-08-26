package com.nowcoder.community.service;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;

@Service
public class UserService implements CommunityConstant{
    
    @Autowired
    private UserMapper  userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }



    public List<User> findAllUsers(){
        return userMapper.selectAllUsers();
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map=new HashMap<>();
        //user为null
        if(user==null)
        {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        User u=userMapper.selectByName(user.getUsername());
        if(u!=null)
        {
            map.put("usernameMsg","该账号已存在！");
            return map;
        }
        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null)
        {
            map.put("emailMsg","该邮箱已被注册！");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().toString().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月和小时的格式为两个大写字母
        Date date = new Date();//获得当前时间
        String birthday = df.format(date);//将当前时间转换成特定格式的时间字符串，这样便可以插入到数据库中
        user.setCreateTime(birthday);
        userMapper.insertUser(user);

        //激活邮件
        Context context=new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url", url);
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);
        return map;
    }

    public int activation(int userId,String code){
        User user=userMapper.selectById(userId);
        if(user.getStatus()==1)
        {
            return ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode().equals(code))
        {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }
        else
        {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map=new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username))
        {
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password))
        {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        //验证账号
        User user=userMapper.selectByName(username);
        if(user==null)
        {
            map.put("usernameMsg","账号不存在");
            return map;
        }
        //验证是否激活
        if(user.getStatus()==0)
        {
            map.put("usernameMsg","账号尚未激活");
            return map;
        }

        //验证密码
        //System.out.println("...............................");
        //System.out.println(password);
        password=CommunityUtil.md5(password+user.getSalt());
        if(!password.equals(user.getPassword()))//不能用”==“测试是否相等，只能用equals，因为“==”只比较两个地址是否相同，不看字符串是否相同
        {
            //System.out.println("///////////////////////////////");
            //System.out.println(password);
            //System.out.println(user.getPassword());
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired("2022-8-22 18:18:18");
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());

        return map;

    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }


    //忘记密码后修改密码
    public Map<String,Object> resetPassword(String email,String password){
        
        Map<String,Object> map=new HashMap<>();
        if(email==null||email.isEmpty())
        {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        User user=userMapper.selectByEmail(email);
        if(user==null)
        {
            map.put("emailMsg", "邮箱不存在!");
            return map;
        }
        
        if(password==null||password.isEmpty())
        {
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        password=CommunityUtil.md5(password+user.getSalt());
        int id=user.getId();
        userMapper.updatePassword(id, password);
        return map;

    }

    public LoginTicket findLoginTicket(String ticket)
    {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId, headerUrl);
    }

    //记得原密码，并修改密码
    public Map<String,Object> updatePassword(int userId,String formerPassword,String newPassword){
        Map<String,Object> map=new HashMap<>();
        User user=userMapper.selectById(userId);
        formerPassword+=user.getSalt();
        formerPassword=CommunityUtil.md5(formerPassword);
        if(!formerPassword.equals(user.getPassword()))
        {
            map.put("error", "原密码不正确");
            return map;
        }
        newPassword+=user.getSalt();
        newPassword=CommunityUtil.md5(newPassword);
        userMapper.updatePassword(userId, newPassword);
        return map;
    }
}
