package com.xxxx.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.yygh.hosp.repository.ScheduleRepository;
import com.xxxx.yygh.hosp.service.ScheduleService;
import com.xxxx.yygh.model.hosp.Department;
import com.xxxx.yygh.model.hosp.Hospital;
import com.xxxx.yygh.model.hosp.Schedule;
import com.xxxx.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;


    @Override
    public void save(Map<String, Object> paramMap) {
        String mapString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(mapString, Schedule.class);

        Schedule scheduleExists  = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if(scheduleExists !=null){
            scheduleExists.setUpdateTime(new Date());
            scheduleExists.setIsDeleted(0);
            scheduleExists.setStatus(1);
            scheduleRepository.save(scheduleExists);
        }else {
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }


    }

    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);

        //分页和条数
        PageRequest pageable = PageRequest.of(page - 1, limit);
        //查询条件
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnoreCase(true)
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        //example对象
        Example<Schedule> example = Example.of(schedule, exampleMatcher);
        //查找
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {

        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(schedule !=null){
            scheduleRepository.deleteById(schedule.getId());
        }
    }
}
