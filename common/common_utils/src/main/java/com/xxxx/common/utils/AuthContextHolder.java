package com.xxxx.common.utils;

import com.xxxx.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

//获取当前用户信息工具类
public class AuthContextHolder {

    /**
     * 获取当前用户id
     */
    public static Long getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = null;
        try {
            userId = JwtHelper.getUserId(token);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return userId;
    }
    /**
     * 获取当前用户名称
     */
    public static String getUserName(HttpServletRequest request){
        String token = request.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
} 
