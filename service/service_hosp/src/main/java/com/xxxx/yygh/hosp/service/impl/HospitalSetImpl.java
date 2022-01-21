package com.xxxx.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.yygh.hosp.mapper.HospitalSetMapper;
import com.xxxx.yygh.hosp.service.HospitalSetService;
import com.xxxx.yygh.model.hosp.HospitalSet;
import org.springframework.stereotype.Service;

@Service
public class HospitalSetImpl extends ServiceImpl<HospitalSetMapper,HospitalSet> implements HospitalSetService {

    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet.getSignKey();
    }
}

