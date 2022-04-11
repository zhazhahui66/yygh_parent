package com.xxxx.yygh.cmn.controller;

import com.xxxx.common.result.Result;
import com.xxxx.common.utils.AuthContextHolder;
import com.xxxx.yygh.model.user.UserInfo;
import com.xxxx.yygh.cmn.service.UserInfoService;
import com.xxxx.yygh.vo.user.LoginVo;
import com.xxxx.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String,Object> result =  userInfoService.loginUser(loginVo);
        return Result.ok(result);
    }

    /**
     * 用户认证接口
     */
    @PostMapping("/auth/saveuserAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){


        //传递两个参数  用户id  认证数据vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);

        return Result.ok();
    }

    /**
     * 获取用户信息接口
     */

    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);

        UserInfo userInfo = userInfoService.getById(userId);
         return Result.ok(userInfo);

    }
}
