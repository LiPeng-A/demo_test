package com.leyou.user.web;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.config.JwtProperties;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.BindException;
import java.util.stream.Collectors;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProp;

    /**
     * 检验数据
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,
                                             @PathVariable("type") Integer type
    ) {
        return ResponseEntity.ok(userService.checkData(data, type));
    }

    /**
     * 发送短信
     *
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam(value = "phone", required = true) String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册用户
     *
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam(value = "code") String code) {
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(
            @RequestParam(value = "username",required = true)String username,
            @RequestParam(value = "password",required = true)String password
    ) {
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }

    /**
     * 退出登录
     * @return
     */
    @PutMapping("logout")
    public ResponseEntity<Void> logoutUser(@CookieValue("LY-TOKEN")String token,
                                           HttpServletRequest request,
                                           HttpServletResponse response

    ){
        String newToken=null;
        CookieUtils.newBuilder(response).httpOnly().request(request).setPath("/")
                .build(jwtProp.getCookieName(),newToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
