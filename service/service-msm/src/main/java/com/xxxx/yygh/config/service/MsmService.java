package com.xxxx.yygh.config.service;

import com.xxxx.yygh.vo.msm.MsmVo;

import javax.servlet.http.HttpSession;

public interface MsmService {
    boolean send(HttpSession session,String phone,int mobile_code);
    boolean send(HttpSession session,MsmVo msmVo);
}
