package com.xxxx.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.yygh.model.hosp.HospitalSet;

public interface HospitalSetService  extends IService<HospitalSet> {
    String getSignKey(String hoscode);
}
