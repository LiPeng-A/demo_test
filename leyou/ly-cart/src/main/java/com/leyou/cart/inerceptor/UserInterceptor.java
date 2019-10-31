package com.leyou.cart.inerceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProp;

    private static final ThreadLocal<UserInfo> tl=new ThreadLocal<>();

    public UserInterceptor(JwtProperties jwtProp) {
        this.jwtProp=jwtProp;
    }

    //前置拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //获取用户的token
        String token = CookieUtils.getCookieValue(request, jwtProp.getCookieName());
        //解析token
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey());
            //传递用户信息
            tl.set(userInfo);
            //放行
            return true;
        } catch (Exception e) {
            log.error("{购物车服务} 解析用户身份失败",e);
            return false;
        }
    }

    //最后视图渲染完毕执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //最后用完数据，清空数据
        tl.remove();
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
