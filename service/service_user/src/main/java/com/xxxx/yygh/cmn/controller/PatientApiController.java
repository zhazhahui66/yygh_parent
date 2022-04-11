package com.xxxx.yygh.cmn.controller;

import com.xxxx.common.result.Result;
import com.xxxx.common.utils.AuthContextHolder;
import com.xxxx.yygh.model.user.Patient;
import com.xxxx.yygh.cmn.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Api(tags = "就诊人管理接口")
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Autowired
    private PatientService patientService;

    //获取就诊人列表
    @ApiOperation(value = "获取就诊人列表")
    @GetMapping("/auth/findAll")
    public Result findAll(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);

        List<Patient> list =  patientService.findAllByUserId(userId);
        return Result.ok(list);
    }

    //添加就诊人
    @ApiOperation(value = "添加就诊人")
    @PostMapping("/auth/save")
    public Result savePatient(@RequestBody Patient patient,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok(patient);
    }

    //根据ID获取就诊人信息
    @ApiOperation(value = "根据ID获取就诊人信息")
    @GetMapping("/auth/get/{id}")
    public Result getPatient(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }


    //删除就诊人信息
    @ApiOperation(value = "删除就诊人信息")
    @DeleteMapping("/auth/remove/{id}")
    public Result removePatient(@PathVariable Long id ){
        patientService.removeById(id);
        return Result.ok();
    }

    //修改就诊人信息
    @ApiOperation(value = "修改就诊人信息")
    @PostMapping("/auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return Result.ok();
    }

    //根据就诊人id获取就诊人信息
    @GetMapping("/inner/get/{id}")
    public Patient getPatientOrder(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return patient;
    }
}
