package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    //从request中获取需要的信息
    public static String getValue(HttpServletRequest request,String name)
    {
        if(name==null||request==null)
        {
            throw new IllegalArgumentException("参数为空");
        }

        Cookie[] cookies=request.getCookies();

        if(cookies!=null)
        {
            for(Cookie cookie:cookies)
            {
                if(name.equals(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
