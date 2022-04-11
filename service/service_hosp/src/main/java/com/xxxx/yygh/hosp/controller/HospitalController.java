package com.xxxx.yygh.hosp.controller;

import com.xxxx.common.result.Result;
import com.xxxx.yygh.hosp.service.HospitalService;
import com.xxxx.yygh.model.hosp.Hospital;
import com.xxxx.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/hospital")

@Api(tags = "医院")
@Slf4j
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    //医院列表(分页查询)
    @ApiOperation(value = "医院列表(分页查询)")
    @GetMapping("/list/{page}/{limit}")
    public Result listHosp(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pageModel = hospitalService.selectHospPage(page,limit,hospitalQueryVo);
        List<Hospital> content = pageModel.getContent();
        Hospital hospital = content.get(0);
        log.info(hospital.toString());
        return Result.ok(pageModel);
    }

    //更新医院上线状态
    @ApiOperation(value = "更新医院上线状态")
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable String id ,@PathVariable Integer status){

        hospitalService.updateHospStatus(id,status);
        return Result.ok();
    }

    //医院详情信息
    @ApiOperation(value = "医院详情信息")
    @GetMapping("/showHospDetail/{id}")
    public Result showHospDetail(@PathVariable String id){
        Map<String,Object> map =  hospitalService.getHospById(id);
        return Result.ok(map);
    }
}
