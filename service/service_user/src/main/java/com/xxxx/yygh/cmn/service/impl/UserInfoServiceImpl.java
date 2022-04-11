package com.xxxx.yygh.cmn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.common.helper.JwtHelper;
import com.xxxx.common.result.ResultCodeEnum;
import com.xxxx.common.result.exception.YyghException;
import com.xxxx.yygh.enums.AuthStatusEnum;
import com.xxxx.yygh.model.user.Patient;
import com.xxxx.yygh.cmn.mapper.UserInfoMapper;
import com.xxxx.yygh.model.user.UserInfo;
import com.xxxx.yygh.cmn.service.PatientService;
import com.xxxx.yygh.cmn.service.UserInfoService;
import com.xxxx.yygh.vo.user.LoginVo;
import com.xxxx.yygh.vo.user.UserAuthVo;
import com.xxxx.yygh.vo.user.UserInfoQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class UserInfoServiceImpl  extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService{
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //从loginVo中获取输入的手机号，和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //判断手机号和验证码是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //判断手机验证码和输入的验证码是否一致
        Integer  moblie_code = (Integer) redisTemplate.opsForValue().get(phone);
        String moblie_code2 = Integer.toString(moblie_code);
        if(!code.equals(moblie_code2)){
            log.info(code+":"+moblie_code2);
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        //根据是否第一次登录：根据手机号查询数据库，如果不存在相同手机号就是第一次登录
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        //第一次登录
        if(userInfo == null){
            userInfo = new UserInfo();
            userInfo.setName("");
            userInfo.setStatus(1);
            userInfo.setPhone(phone);
            baseMapper.insert(userInfo);
        }
        //判断账户是否被禁用
        if(userInfo.getStatus() ==0){
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //返回登录信息
        //返回登录用户名
        //返回token信息
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)){
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        map.put("name",name);
        map.put("token", JwtHelper.createToken(userInfo.getId(),name));

        return map;
    }

    /**
     * 用户认证
     * @param userId
     * @param userAuthVo
     */
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
         //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        if(userInfo!=null){
            userInfo.setName(userAuthVo.getName());
            userInfo.setCertificatesType(userAuthVo.getCertificatesType());
            userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
            userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
            userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
            //进行信息更新
            baseMapper.updateById(userInfo);
        }


    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //UserInfoQueryVo获取值
        String name = userInfoQueryVo.getKeyword();//用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus();//认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();//开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();//结束时间

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        //对条件值进行非空判断
        if(!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)){
            wrapper.eq("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le("create_time",createTimeEnd);
        }
        if(!StringUtils.isEmpty(createTimeBegin)){
            wrapper.ge("create_time",createTimeBegin);
        }
        Page<UserInfo> pages = baseMapper.selectPage(pageParam, wrapper);

        //封装对应值
        pages.getRecords().forEach(item ->{
            this.packageUserInfo(item);
        });

        return pages;
    }

    @Override
    public void lock(Long userId, Integer status) {
        if(status.intValue() == 0|| status.intValue() ==1){
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> show(Long userId) {
        HashMap<String, Object> map = new HashMap<>();
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
        map.put("userInfo",userInfo);

        List<Patient> patientList = patientService.findAllByUserId(userId);
        map.put("patientList",patientList);

        return map;
    }
    //用户认证审批功能
    @Override
    public void approval(Long userId, Integer authStatus) {
        //认证审批  2通过  -1不通过
        if(authStatus.intValue() == 2 || authStatus.intValue() == -1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }

    }

    private UserInfo packageUserInfo(UserInfo userInfo) {
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        userInfo.getParam().put("statusString",userInfo.getStatus()==0 ? "锁定" :"正常");
        return userInfo;
    }
}
