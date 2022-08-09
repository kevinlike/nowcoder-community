package com.nowcoder.community.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.nowcoder.community.service.AlphaService;

@Controller
@RequestMapping("/alpha")//下列类的访问名
public class AlphaController {

    @Autowired
    private AlphaService alphaService;



    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){

        return "<h1>Hello Spring Boot.</h1>";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getDate(){
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request,HttpServletResponse response) throws IOException{
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration=request.getHeaderNames();
        while(enumeration.hasMoreElements())
        {
            String name=enumeration.nextElement();
            String value=request.getHeader(name);
            System.out.println(name+":"+value);
        }
        System.out.println(request.getParameter("code"));

        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer=response.getWriter();
        writer.write("<h1>kevin's web</h1>");
    }

    //GET请求
    // /students?current=1&limit=20
    @RequestMapping(path="/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name="current",required = false,defaultValue = "1")int current,
            @RequestParam(name="limit",required = false,defaultValue = "10")int limit) {
        System.out.println(current);
        System.out.println(limit);
        String tem="students";
        return tem;
    }

    // /student/123   直接在路径中获取参数
    @RequestMapping(path="/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }

    //POST请求
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "succeed";
    }

    //响应HTML数据
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    //这里不使用@ResponseBody 默认返回的是整个html网页
    public ModelAndView getTeacher(){
        ModelAndView mav=new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", "28");
        mav.setViewName("/demo/view");//找到/templates/demo/view.html并填入数据
        return mav;
    }

    //响应hmtl的另一种方法
    @RequestMapping(path = "/school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name", "北京大学");
        model.addAttribute("age", "80");
        return "/demo/view";
    }

    //响应json数据，多见于异步请求中
    //java对象 -> json ->js对象
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String,Object> emp=new HashMap<>();
        emp.put("name", "张三");
        emp.put("age","23");
        emp.put("salary", "16000.00");
        return emp;
    }

    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getEmps(){
        List<Map<String,Object>> list=new ArrayList<>();
        for(int i=0;i<10;i++)
        {
            Map<String,Object> emp=new HashMap<>();
            emp.put("name", "张三"+i);
            emp.put("age","23");
            emp.put("salary", "16000.00");
            list.add(emp);
        }
        
        return list;
    }
}
