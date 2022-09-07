package com.nowcoder.community.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.nowcoder.community.entity.LoginTicket;

@Mapper
@Deprecated//不推荐使用
public interface LoginTicketMapper {
    
    @Insert({
        "insert into login_ticket(user_id,ticket,status,expired) ",
        "values(#{userId},#{ticket},#{status},#{expired})"
    })
    //用于自动生成id
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
    
    @Select({
        "select id,user_id,ticket,status,expired ",
        "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);


    //此处只为了示例动态语句的写法，本没必要加if
    @Update({
        "<script>",
        "update login_ticket set status=#{status} where ticket=#{ticket} ",
        "<if test=\"ticket!=null\">",
        "and 1=1",
        "</if>",
        "</script>"
    })
    int updateStatus(String ticket,int status);
}
