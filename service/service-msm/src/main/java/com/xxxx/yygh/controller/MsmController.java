package com.xxxx.yygh.controller;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.xxxx.common.result.Result;
import com.xxxx.common.result.ResultCodeEnum;
import com.xxxx.common.result.exception.YyghException;
import com.xxxx.yygh.config.service.MsmService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/api/msm")
public class MsmController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MsmService msmService;

    @ApiOperation("发送验证码")
    @GetMapping("send/{phone}")
    public Result yzm(HttpSession session ,@PathVariable String phone){

        //从redis判断是否已存在验证码
        String code = (String) redisTemplate.opsForValue().get(phone);
        if(code !=null){
            return Result.ok();
        }
        int mobile_code = (int)((Math.random()*9+1)*100000);
        boolean isSend = msmService.send(session,phone,mobile_code);

        if(isSend){
            redisTemplate.opsForValue().set(phone,mobile_code);
            return Result.ok();
        }else {
            return Result.fail("发送短信失败");
        }
    }

}
