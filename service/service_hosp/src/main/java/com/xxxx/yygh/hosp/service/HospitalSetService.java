package com.xxxx.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.yygh.model.hosp.HospitalSet;
import com.xxxx.yygh.vo.order.SignInfoVo;

public interface HospitalSetService  extends IService<HospitalSet> {
    String getSignKey(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
