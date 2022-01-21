package com.xxxx.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxxx.common.result.Result;
import com.xxxx.yygh.hosp.service.HospitalSetService;
import com.xxxx.yygh.hosp.utils.MD5;
import com.xxxx.yygh.model.hosp.HospitalSet;
import com.xxxx.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
@CrossOrigin
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Api(tags = "医院设置管理")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;
    @ApiOperation(value = "获取所有医院设置信息")
    @GetMapping("/findAll")
    public Result findAllHospitalSet(){
        List<HospitalSet> list = hospitalSetService.list();
        Result<List> result = Result.ok(list);
        return result;
    }
    @ApiOperation(value = "删除医院设置信息")
    @DeleteMapping("{id}")
    public Result removeHospSet(@PathVariable Long id){
        boolean flag=  hospitalSetService.removeById(id);
        if(flag){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //3条件查询带分页
    @ApiOperation(value = "查询带分页")
    @PostMapping("findPageHospSet/{current}/{limit}")
    public Result findPageHospSet(@PathVariable long current,
                                    @PathVariable long limit,
                    @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){

        //创建page对象，传递当前页，每页记录数
        Page<HospitalSet> page = new Page<>(current,limit);
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();

        if(!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())){
            wrapper.like("hosname",hospitalSetQueryVo.getHosname());
        }
        if(!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())){
            wrapper.eq("hoscode",hospitalSetQueryVo.getHoscode());
        }
        //调用方法实现分页查询
        Page<HospitalSet> pageHospitalSet = hospitalSetService.page(page, wrapper);

        return Result.ok(pageHospitalSet);
    }

    //4.添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        //设置状态 1使用 0 不能使用
        hospitalSet.setStatus(1);

        //签名密钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));

        //调用service
        boolean save = hospitalSetService.saveOrUpdate(hospitalSet);
        if(save){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    //5.根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("getHospSet/{id}")
    public Result getHospSet(@PathVariable long id){

        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    //6.修改医院设置
    @ApiOperation(value = "修改医院设置")
    @PostMapping("updateHospitalSet")
    public Result updateHospSet(@RequestBody HospitalSet hospitalSet){
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //7.批量删除医院设置
    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("batchRemove")
    public  Result batchRemoveHospitalSet(@RequestBody List<Long> idList){
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    //8.医院锁定和解锁
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,@PathVariable Integer status){
        //1.根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //设置状态
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();

    }

    //9.发送签名密钥
    @PutMapping("sendKey/{id}")
    public Result sendKey(@PathVariable Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //todo 发送短信
        return Result.ok();


    }
}
