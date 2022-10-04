package com.nowcoder.community.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nowcoder.community.service.DataService;

@Controller
public class DataController {
    
    @Autowired
    private DataService dataService;

    //打开统计页面
    //getUV和getDAU使用了转发的方法转发给getDataPage，这两个方法都是post方法，而转发不能改变方法类型，所以此方法也必须支持post
    @RequestMapping(path="/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    //统计网站UV
    @RequestMapping(path="/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,Model model){
        long uv=dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);

        //可以直接传回模版
        //return "/site/admin/data";
        //也可以使用forward方法，表示此函数处理了一半，继续传递给一个平级的方法继续处理
        return "forward:/data";
    }

    //统计网站DAU
    @RequestMapping(path="/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end,Model model){
        long dau=dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);

        //可以直接传回模版
        //return "/site/admin/data";
        //也可以使用forward方法，表示此函数处理了一半，继续传递给一个平级的方法继续处理
        return "forward:/data";
    }

}
