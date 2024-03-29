package com.nowcoder.community.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant{
    

    private static final Logger logger=LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired//自定义的注解，表示只有在登录时才能访问
    @RequestMapping(path="/setting",method=RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired//自定义的注解，表示只有在登录时才能访问
    @RequestMapping(path="/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage,Model model){
        if(headerImage==null)
        {
            model.addAttribute("error", "无图片");
            return "/site/setting";
        }

        String fileName=headerImage.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix))
        {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        //生成随机文件名
        fileName=CommunityUtil.generateUUID()+suffix;
        //确定文件存放路径（新建一个空文件）
        File dest=new File(uploadPath+"/"+fileName);
        try {
            //将用户的文件存入刚刚新建的空文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常",e);
        }

        //更新当前用户的头像的路径（web访问路径）
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";


    }

    @RequestMapping(path="/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName,HttpServletResponse response){
        //服务器存放路径
        fileName=uploadPath+"/"+fileName;
        //文件后缀
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);

        try(
            FileInputStream fis=new FileInputStream(fileName);
            OutputStream os=response.getOutputStream();
        ){
            byte[] buffer=new byte[1024];
            int b=0;
            while((b=fis.read(buffer))!=-1)
            {
                os.write(buffer,0,b);
            }
        }catch(IOException e){
            logger.error("读取失败："+e.getMessage());
        }
    }

    @RequestMapping(path="/updatePassword",method = RequestMethod.POST)
    public String getHeader(Model model,String formerPassword,String newPassword){
        User user=hostHolder.getUser();
        int userId=user.getId();
        Map<String,Object> map=userService.updatePassword(userId, formerPassword, newPassword);
        if(!map.isEmpty())
        {
            model.addAttribute("codeError", map.get("error"));
            return "/site/setting";
        }
        return "redirect:/login";
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount=likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //登录用户
        User loginUser=hostHolder.getUser();

        //关注状态
        boolean hasFollowed=loginUser==null?false:followService.hasFollowed(loginUser.getId(), ENTITY_TYPE_USER, userId);
        model.addAttribute("hasFollowed", hasFollowed);
        //粉丝数量
        long followerCount=followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //关注的目标数量
        long followeeCount=followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        return "/site/profile";
    }
}
