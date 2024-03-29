package com.nowcoder.community.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    
    public static String date2String(Date date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月和小时的格式为两个大写字母
        String dateString = df.format(date);//将当前时间转换成特定格式的时间字符串，这样便可以插入到数据库中
        return dateString;
    }
    
}
