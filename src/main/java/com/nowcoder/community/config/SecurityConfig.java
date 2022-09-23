package com.nowcoder.community.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant{
    @Autowired
    private UserService userService;

    //忽略对静态资源的拦截
    @Override
    public void configure(WebSecurity web) throws Exception {
        //需要忽略的路径（主要是静态资源）
        web.ignoring().antMatchers("/resources/**");
    }

    // //认证处理
    // /* 
    //  * AuthenticationManager：认证的核心接口
    //  * AuthenticationManagerBuilder：用于构建AuthenticationManager对象的工具
    //  * ProviderManager:AuthenticationManager接口的默认实现类
    //  */
    // @Override
    // protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    //     //内置的认证规则
    //     //auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder("123456"));

    //     //自定义的认证规则
    //     //AuthenticationProvider:ProviderManager持有一组AuthenticationProvider，每个AuthenticationProvider负责一种认证
    //     //委托模式：ProviderManager将认证委托给了AuthenticationProvider
    //     auth.authenticationProvider(new AuthenticationProvider() {

    //         //Authentication：用于封装认证信息（账号，密码等）的接口，不同的实现类代表不同类型的认证信息，例如a接口是账号密码认证，b是qq认证等
    //         @Override
    //         public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    //             String username=authentication.getName();
    //             String password=(String) authentication.getCredentials();
    //             User user =userService.findUserByName(username);
    //             if(user==null){
    //                 throw new UsernameNotFoundException("账号不存在");
    //             }
    //             password=CommunityUtil.md5(password+user.getSalt());
    //             if(!user.getPassword().equals(password)){
    //                 throw new BadCredentialsException("密码不正确！");
    //             }
    //             /* 
    //              * @param
    //              * principal：主要信息
    //              * credentials:证书（密码）
    //              * authorities:权限
    //              */
    //             return new UsernamePasswordAuthenticationToken(user, user.getPassword(),user.getAuthorities());
    //         }

    //         //返回当前的接口（Authentication）支持的认证类型
    //         @Override
    //         public boolean supports(Class<?> authentication) {
    //             //UsernamePasswordAuthenticationToken:Authentication接口的常用实现类，表示当前接口支持的是账号密码的验证模式，而不是微信qq验证码等
    //             return UsernamePasswordAuthenticationToken.class.equals(authentication);
    //         }
    //     });
    // }

    //授权操作
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // //登录相关配置
        // http.formLogin()
        //         .loginPage("/loginPage")//登录页面
        //         .loginProcessingUrl("/login")//登录功能的请求路径
        //         //登录成功后
        //         .successHandler(new AuthenticationSuccessHandler() {
        //             @Override
        //             public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        //                     Authentication authentication) throws IOException, ServletException {
        //                 response.sendRedirect(request.getContextPath()+"/index");//登录成功后重定向到首页
                        
        //             }
        //         })
        //         //登录失败后
        //         .failureHandler(new AuthenticationFailureHandler() {
        //             @Override
        //             public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        //                     AuthenticationException exception) throws IOException, ServletException {
        //                 request.setAttribute("error", exception.getMessage());
        //                 request.getRequestDispatcher("/login").forward(request, response);//登录失败后转发到登录页面
        //             }
        //         });
        // //退出登录的相关配置
        // http.logout()
        //         .logoutUrl("/logout")//退出后的页面
        //         .logoutSuccessHandler(new LogoutSuccessHandler() {
        //             @Override
        //             public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
        //                     Authentication authentication) throws IOException, ServletException {
        //                 response.sendRedirect(request.getContextPath()+"/index");
        //             }
        //         });
        //授权配置
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow")
                    .hasAnyAuthority(AUTHORITY_USER,AUTHORITY_ADMIN,AUTHORITY_MODERATOR)//不同页面需要的权限设置
                .anyRequest().permitAll()//其余所有请求都允许
                .and().csrf().disable();
                //.antMatchers("/admin").hasAnyAuthority("ADMIN")//不同页面需要的权限
                //.and().exceptionHandling().accessDeniedPage("/denied");//权限与页面不匹配时跳转的页面
        //权限不够时的操作
        http.exceptionHandling()
                //未登录时的处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response,
                            AuthenticationException authException) throws IOException, ServletException {
                        /* 
                         * 未登录时需要提示用户先进行登录才能访问特定内容
                         * 如果用户采用同步请求（请求一个网页），那么直接让他跳转到登录页面即可
                         * 但是如果用户采用的是异步请求（请求一个json），那么不能直接跳转网页，需要返回一个json格式数据
                         */
                        String xRequestedWith= request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer=response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录"));
                        }
                        else{
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                        
                    }
                })
                //权限不足时的处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response,
                            AccessDeniedException accessDeniedException) throws IOException, ServletException {

                            String xRequestedWith= request.getHeader("x-requested-with");
                            if("XMLHttpRequest".equals(xRequestedWith)){
                                response.setContentType("application/plain;charset=utf-8");
                                PrintWriter writer=response.getWriter();
                                writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限"));
                            }
                            else{
                                response.sendRedirect(request.getContextPath()+"/denied");
                            }
                        
                    }
                });

        //Security底层默认拦截/logout请求，进行退出处理
        //覆盖他的逻辑才能执行我们自己的退出代码
        //将他的拦截路径由默认的/logout改为/securitylogout，这个新路径不存在，所以就不会被拦截到
        http.logout().logoutUrl("/securitylogout");


        // //增加Filter，处理验证码
        // http.addFilterBefore(new Filter() {
        //     @Override
        //     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        //             throws IOException, ServletException {
        //         HttpServletRequest request=(HttpServletRequest) servletRequest;
        //         HttpServletResponse response=(HttpServletResponse) servletResponse;
        //         if(request.getServletPath().equals("/login")){
        //             String verifyCode=request.getParameter("verifyCode");
        //             if(verifyCode==null||!verifyCode.equals("1234")){
        //                 request.setAttribute("error","验证码错误");
        //                 request.getRequestDispatcher("/loginpage").forward(request, response);
        //                 return;
        //             }
        //         }
        //         //让请求继续向下执行
        //         chain.doFilter(request, response);
                
        //     }
        // }, UsernamePasswordAuthenticationFilter.class);

        // //“记住我”功能
        // http.rememberMe()
        //         .tokenRepository(new InMemoryTokenRepositoryImpl())//保存的位置是内存，也可以自己修改成redis
        //         .tokenValiditySeconds(3600*24)//记住的时长
        //         .userDetailsService(userService);//保存用户信息，用于认证
    }
    
}
