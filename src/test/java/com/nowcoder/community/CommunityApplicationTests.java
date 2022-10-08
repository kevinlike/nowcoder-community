package com.nowcoder.community;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;




@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class CommunityApplicationTests implements ApplicationContextAware{


	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}

	//@Test
	public void testApplicationContext(){
		System.out.println("===================||++++++++++++++++++");
		System.out.println(applicationContext);

		AlphaDao alphaDao=applicationContext.getBean(AlphaDao.class);
		System.out.println(alphaDao.select());

		alphaDao=applicationContext.getBean("alphaHibernate",AlphaDao.class);
		System.out.println(alphaDao.select());
	}

	//@Test
	public void testBeanManagement(){
		AlphaService alphaService=applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
	}

	//@Test
	public void testBeanConfig(){
		SimpleDateFormat simpleDateFormat = 
				applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(System.currentTimeMillis()));
	}

	@Autowired
	@Qualifier("alphaHibernate")
	private AlphaDao alphaDao;

	@Autowired
	private AlphaService alphaService;

	@Autowired
	private SimpleDateFormat simpleDateFormat;
	//@Test
	public void testDI(){
		System.out.println("===================||------------------");
		System.out.println(alphaDao);
		System.out.println(alphaService);
		System.out.println(simpleDateFormat);
	}


	@Autowired
	private UserMapper userMapper;

	//测试设置用户密码
	//@Test
	public void testUserSetPassword(){
		String username="xixi";
		User user =userMapper.selectByName(username);
		String password="123"+user.getSalt();
		password=CommunityUtil.md5(password);
		System.out.println(password);
		user.setPassword(password);
		
	}

	//测试用户密码是否与数据库匹配
	//@Test
	public void testUserPassword(){
		String password="123f9ead";
		password=CommunityUtil.md5(password);
		User user =userMapper.selectByName("xixi");
		System.out.println("...............................");
        System.out.println(password);
        if(!password.equals(user.getPassword()))
        {
            System.out.println("///////////////////////////////");
            System.out.println(password);
            System.out.println(user.getPassword());
        }
		else
		{
			System.out.println("密码匹配");
		}
	}

	@Test
	public void test(){
		//String numbers="01234";
		//numbers=numbers.substring(numbers.lastIndexOf("1"));
		//System.out.println(numbers);
		System.out.println("---------------------------");
		String time="2022-10-05 22:35:00";
		try {
			Date date=DateUtils.parseDate(time, "yyyy-MM-dd HH:mm:ss");
			System.out.println(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

}
