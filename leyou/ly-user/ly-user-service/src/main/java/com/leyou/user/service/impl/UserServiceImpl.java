package com.leyou.user.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final String Key_prefix="user:verify:phone:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 数据校验
     * @param data
     * @param type
     * @return
     */
    @Override
    public Boolean checkData(String data, Integer type) {
        //判断数据类型
        User checkUser = new User();
        switch (type)
        {
            case 1: //是用户名
                checkUser.setUsername(data);
                break;
            case 2: //是手机号码
                checkUser.setPhone(data);
                break;
            default: //不是这两种类型
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }

        return userMapper.selectCount(checkUser)==0;
    }

    /**
     * 发送短信
     * @param phone
     */
    @Override
    public void sendCode(String phone) {
        //生成key
        String key=Key_prefix +phone;
        // 生成验证码
        String code= NumberUtils.generateCode(6);
        HashMap<String, String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        //发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);

        //保存验证码，以便之后的注册校验,保存到redis中,验证码存在时长为5分钟
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);

    }

    @Transactional
    @Override
    public void register(User user, String code) {
        //从redis中取出验证码
        String cacheCode = redisTemplate.opsForValue().get(Key_prefix + user.getPhone());
        //校验验证码
        if(!StringUtils.equals(code,cacheCode)){
            //验证码不正确，抛出异常，给与页面友好提示
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //对密码进行加密
        String encodePassword = CodecUtils.passwordBcryptEncode(user.getUsername(), user.getPassword());
        user.setPassword(encodePassword);
        //将数据写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);
    }

    @Override
    public User queryUserByUsernameAndPassword(String username, String password) {
        //查询用户
        User user = new User();
        user.setUsername(username);
        User user1 = userMapper.selectOne(user);
        //校验是否存在
        if(user1==null)
        {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        if (!CodecUtils.passwordBcryptDecode(username+password, user1.getPassword())) {
            //用户信息不正确
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //用户名和密码正确
        return user1;
    }

}
