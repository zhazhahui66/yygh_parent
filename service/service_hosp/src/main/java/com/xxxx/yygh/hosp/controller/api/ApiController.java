package com.xxxx.yygh.hosp.controller.api;

import com.xxxx.common.result.Result;
import com.xxxx.common.result.ResultCodeEnum;
import com.xxxx.common.result.exception.YyghException;
import com.xxxx.yygh.common.helper.HttpRequestHelper;
import com.xxxx.yygh.hosp.service.DepartmentService;
import com.xxxx.yygh.hosp.service.HospitalService;
import com.xxxx.yygh.hosp.service.HospitalSetService;
import com.xxxx.yygh.hosp.service.ScheduleService;
import com.xxxx.yygh.hosp.utils.MD5;
import com.xxxx.yygh.model.hosp.Department;
import com.xxxx.yygh.model.hosp.Hospital;
import com.xxxx.yygh.model.hosp.HospitalSet;
import com.xxxx.yygh.model.hosp.Schedule;
import com.xxxx.yygh.vo.hosp.DepartmentQueryVo;
import com.xxxx.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.util.Map;
@Log4j2
@RestController
@RequestMapping("/api/hosp")
@Api(value = "api接口")
public class ApiController {
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;
    //上传医院接口
    @PostMapping("/saveHospital")
    public Result saveHosp(HttpServletRequest request){
        //获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        isSingKey(paramMap);
        //图片的传输转换
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        paramMap.put("logoData",logoData);

        hospitalService.save(paramMap);
        return Result.ok();

    }

    private void isSingKey(Map<String, Object> paramMap) {
        //获取医院传过来的签名，进行md5加密
        String hospSign = (String) paramMap.get("sign");
        String hoscode = (String) paramMap.get("hoscode");

        String signKey = hospitalSetService.getSignKey(hoscode);
        log.info("数据库加密后signKey：  "+signKey);
        log.info("医院signKey：  "+hospSign);
        String sign = MD5.encrypt(signKey);
        log.info("数据库加密后signKey：  "+sign);

        if(!hospSign.equals(sign)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }

    //上传科室接口
    @PostMapping("/saveDepartment")
    @ApiOperation(value = "上传科室接口")
    public Result saveDepartment(HttpServletRequest request){
        //获取传递过来的科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //校验singkey
        isSingKey(paramMap);

        departmentService.save(paramMap);
        return Result.ok();
    }

    //查询科室
    @PostMapping("/department/list")
    public Result findDepartment(HttpServletRequest request){
        //获取传递过来科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //医院编号
        String hoscode = (String) paramMap.get("hoscode");

        //当前页和每页记录数
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 3 : Integer.parseInt((String) paramMap.get("limit"));

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        isSingKey(paramMap);

        Page<Department> pageModel =  departmentService.findPageDepartment(page,limit,departmentQueryVo);
        return Result.ok(pageModel);

    }
    //删除科室接口
    @PostMapping("/department/remove")
    public Result removeDepartment(HttpServletRequest request){
        //获取传递过来信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        isSingKey(paramMap);

        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }

    //查询医院接口
    @PostMapping("hospital/show")
    @ApiOperation(value = "获取医院信息")
    public Result hospitalShow(HttpServletRequest request){
        //获取传递过来信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        String hoscode = (String) paramMap.get("hoscode");

        isSingKey(paramMap);

        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    //上传排班接口
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        //获取传递过来信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        isSingKey(paramMap);
        scheduleService.save(paramMap);
        return Result.ok();
    }

    //查询排班接口
    @PostMapping("/schedule/list")
    public Result findSchedule(HttpServletRequest request){
        //获取传递过来信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        isSingKey(paramMap);
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String)paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String)paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> pageModel = scheduleService.selectPage(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }

    //删除排班接口
    @PostMapping("schedule/remove")
    public Result remove(HttpServletRequest request){
        //获取传递过来信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院编号和排班编号
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String)paramMap.get("hosScheduleId");

        isSingKey(paramMap);

        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }
}
