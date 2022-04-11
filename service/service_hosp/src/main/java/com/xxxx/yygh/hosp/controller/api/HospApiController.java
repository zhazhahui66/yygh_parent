package com.xxxx.yygh.hosp.controller.api;

import com.xxxx.common.result.Result;
import com.xxxx.yygh.hosp.service.DepartmentService;
import com.xxxx.yygh.hosp.service.HospitalService;
import com.xxxx.yygh.hosp.service.ScheduleService;
import com.xxxx.yygh.model.hosp.Hospital;
import com.xxxx.yygh.model.hosp.Schedule;
import com.xxxx.yygh.vo.hosp.DepartmentVo;
import com.xxxx.yygh.vo.hosp.HospitalQueryVo;
import com.xxxx.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
public class HospApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;
    @ApiOperation(value = "查询医院列表")
    @GetMapping("findHospList/{page}/{limit}")
    public Result findHospList(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitals) ;
    }

    @ApiOperation(value = "根据医院名称查询")
    @GetMapping("/findByHosName/{hosname}")
    public Result findByHostName(@PathVariable String hosname){
        List<Hospital> list = hospitalService.findByHosname(hosname);
        return Result.ok(list);
    }

    @ApiOperation(value = "根据医院编号名称获取所有科室信息")
    @GetMapping("/department/{hoscode}")
    public Result index(@PathVariable String hoscode){
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return Result.ok(list);
    }

    @ApiOperation(value = "根据医院编号获取医院预约挂号详情")
    @GetMapping("/findHospitalDetail/{hoscode}")
    public Result bookingRulesList(@PathVariable String hoscode) {
        Map<String,Object> map = hospitalService.getBookingRulesPage(hoscode);
        return Result.ok(map);
    }




    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("/auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingScheduleRule(@PathVariable Integer page,
                                         @PathVariable Integer limit,
                                         @PathVariable String hoscode,
                                         @PathVariable String depcode){
        Map<String, Object> map = scheduleService.getBookingScheduleRule(page,limit,hoscode,depcode);
        return Result.ok(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("/auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode,
                                   @PathVariable String depcode,
                                   @PathVariable String workDate){
        return Result.ok(scheduleService.getDetailSchedule(hoscode,depcode,workDate));
    }


    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("/getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId){
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return Result.ok(schedule);
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getsScheduleOrderVo(
            @PathVariable("scheduleId")String scheduleId){
        return scheduleService.getScheduleOrderVo(scheduleId);
    }
}
