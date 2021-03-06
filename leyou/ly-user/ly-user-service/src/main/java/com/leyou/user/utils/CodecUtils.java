package com.leyou.user.utils;
 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class CodecUtils {
 
    public static String passwordBcryptEncode(String username,String password){
 
        return new BCryptPasswordEncoder().encode(username + password);
    }
 
    public static Boolean passwordBcryptDecode(String rawPassword,String encodePassword){
        return new BCryptPasswordEncoder().matches(rawPassword,encodePassword);
    }
}