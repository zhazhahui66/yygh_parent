package com.xxxx.yygh.hosp.client;

import com.xxxx.yygh.vo.hosp.ScheduleOrderVo;
import com.xxxx.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-hosp")
@Repository
public interface HospitalFeignClient {
    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getsScheduleOrderVo(
            @PathVariable("scheduleId")String scheduleId);

    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("/api/hosp/hospital/inner/getSignInfo/{hoscode}")
    public SignInfoVo getSignInfo(@PathVariable("hoscode") String hoscode);
}
