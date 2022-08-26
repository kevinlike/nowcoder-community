package com.nowcoder.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.nowcoder.community.entity.User;

@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    //下列selectAllUsers()方法 由Kevin自主开发，用于选出所有用户信息
    List<User> selectAllUsers();

    int insertUser(User user);

    int updateStatus(int id,int status);

    int updateHeader(int id,String headerUrl);

    int updatePassword(int id,String password);

    int updateActivationCode(int id,String activationCode);
}
