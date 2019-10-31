package com.leyou.user.service;

import com.leyou.user.pojo.User;

public interface UserService {

    //检验数据
    Boolean checkData(String data, Integer type);

    //发送随机验证码
    void sendCode(String phone);

    //注册用户
    void register(User user, String code);

    //根据用户名和密码查询用户
    User queryUserByUsernameAndPassword(String username, String password);

}
