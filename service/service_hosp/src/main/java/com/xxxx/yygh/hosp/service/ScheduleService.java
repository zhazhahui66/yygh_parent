package com.xxxx.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.common.result.Result;
import com.xxxx.yygh.model.hosp.Schedule;
import com.xxxx.yygh.vo.hosp.ScheduleOrderVo;
import com.xxxx.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void save(Map<String, Object> paramMap);

    Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    Schedule getScheduleById(String scheduleId);

    /**
     * 根据排班id获取预约下单数据
     * @param scheduleId 排班id
     * @return 排班数据
     */
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**
     * 修改排班
     * @param schedule
     */
    void update(Schedule schedule);
}
