package com.xxxx.yygh.cmn.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxxx.common.result.Result;
import com.xxxx.yygh.hosp.service.ScheduleService;
import com.xxxx.yygh.model.user.UserInfo;
import com.xxxx.yygh.cmn.service.UserInfoService;
import com.xxxx.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "条件分页查询用户信息")
    @GetMapping("/{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> pageParam = new Page<>(page,limit);
        IPage<UserInfo> pageModel =  userInfoService.selectPage(pageParam,userInfoQueryVo);

        return Result.ok(pageModel);
    }

    @GetMapping("/lock/{userId}/{status}")
    @ApiOperation(value = "锁定解锁")
    public Result lock(@PathVariable Long userId,@PathVariable Integer status){
        userInfoService.lock(userId,status);
        return Result.ok();
    }

    @GetMapping("/show/{userId}")
    @ApiOperation(value = "详情信息")
    public Result show(@PathVariable Long userId){
        Map<String,Object> map = userInfoService.show(userId);
        return Result.ok(map);
    }

    @GetMapping("/approval/{userId}/{authStatus}")
    @ApiOperation("认证审批")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus){
       userInfoService.approval(userId,authStatus);
       return Result.ok();
    }

}
