package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 登录授权功能
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam(value = "username")String username,
            @RequestParam(value = "password")String password,
            HttpServletResponse response,
            HttpServletRequest request
    ){
        //登录
        String token = authService.login(username, password);
        //  将token写入cookie中
        CookieUtils.newBuilder(response).httpOnly().request(request)
                .build(jwtProperties.getCookieName(),token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 校验用户登录状态
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY-TOKEN") String token,
                                           HttpServletRequest request, HttpServletResponse response
                                           ){
        //解析token
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //重新生成token，写入cookie中，完成刷新token
            String newToken = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            CookieUtils.newBuilder(response).httpOnly().request(request).setPath("/")
                    .build(jwtProperties.getCookieName(),newToken);
            //已登录返回用户信息
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            //token过期，或者token无效 被篡改过
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

}
