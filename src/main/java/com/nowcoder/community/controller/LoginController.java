package com.nowcoder.community.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import com.google.code.kaptcha.Producer;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;

@Controller
public class LoginController implements CommunityConstant{

    private static final Logger logger=org.slf4j.LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path="/register",method=RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }


    @RequestMapping(path="/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path="/register",method=RequestMethod.POST)
    public String register(Model model,User user){
        Map<String,Object> map=userService.register(user);
        if(map==null||map.isEmpty())
        {
            model.addAttribute("msg", "注册成功,请前往邮箱激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        else
        {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }

    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path="/activation/{userId}/{code}",method=RequestMethod.GET)
    public String activation(Model model,@PathVariable("userId") int userId,@PathVariable("code") String code)
    {
        int result=userService.activation(userId, code);
        if(result==ACTIVATION_SUCCESS)
        {
            model.addAttribute("msg", "激活成功！");
            model.addAttribute("target","/login");
        }
        else if(result==ACTIVATION_REPEAT)
        {
            model.addAttribute("msg", "重复激活");
            model.addAttribute("target","/index");
        }
        else
        {
            model.addAttribute("msg", "激活失败！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    //原先将验证码存入session的方法在分布式服务器上会有诸多问题，所以使用redis方法进行重构
    @RequestMapping(path="/kaptcha",method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/* ,HttpSession session*/){
        //生成验证码
        String text=kaptchaProducer.createText();
        BufferedImage image=kaptchaProducer.createImage(text);

        //将验证码存入session
        //session.setAttribute("kaptcha", text);

        //验证码的归属
        String kaptchaOwner =CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);//生存时间
        cookie.setPath(contextPath);//生效范围
        response.addCookie(cookie);
        //将验证码存入redis
        String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text,60,TimeUnit.SECONDS);

        //将图片传给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os=response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (Exception e) {
            logger.error("响应验证码失败："+e.getMessage());
        }

    }

    //重构前：从session中取验证码真实值
    //重构后：从redis中取验证码的值
    @RequestMapping(path="/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,Model model/* ,HttpSession session*/,HttpServletResponse response,@CookieValue("kaptchaOwner") String kaptchaOwner){
        
        //String kaptcha=(String) session.getAttribute("kaptcha");
        if(kaptchaOwner.isBlank()){
            model.addAttribute("codeMsg","cookie中的kaptchaOwner失效");
            return "/site/login";
        }

        String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        String kaptcha=null;
        kaptcha=(String)redisTemplate.opsForValue().get(redisKey);
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        //检查账号密码
        int expiredSeconds=rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map=userService.login(username,password,expiredSeconds);
        if(map.containsKey("ticket"))
        {
            Cookie cookie=new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }
        else
        {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @RequestMapping(path="/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }

    @RequestMapping(path="/getVerificationCode",method=RequestMethod.POST)
    @ResponseBody
    public int getVriationCode(String email,HttpSession session){
        System.out.println(email);
        
        //Map<String,Object> map=userService.getVerificationCode(email);

        if(StringUtils.isBlank(email))
        {
            return 0;//发送失败
            
        }
        else
        {
            String verificationCode=CommunityUtil.generateUUID().toString().substring(0,5);
            session.setAttribute("code", verificationCode);
            Context context=new Context();
            context.setVariable("email", email);
            context.setVariable("verificationCode", verificationCode);
            String content=templateEngine.process("/mail/forget",context);
            mailClient.sendMail(email, "找回密码", content);
            return 1;//发送成功
        }
        
    }

    @RequestMapping(path = "/forget",method = RequestMethod.POST)
    public String checkVerificationCode(String code,String email,String password,Model model,HttpSession session){
        String verificationCode=(String)session.getAttribute("code");
        if(!code.equals(verificationCode))
        {
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/forget";
        }
        Map<String,Object> map=userService.resetPassword(email, password);
        if(!map.isEmpty())
        {
            System.out.println("--------------------");
            model.addAttribute("emailMsg",map.get("emailMsg"));
            
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/forget";
        }
        else
        {
            System.out.println("++++++++++++++++++++");
            return "redirect:/login";
        }
        
    }
     
}
