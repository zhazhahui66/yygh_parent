package com.xxxx.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.yygh.cmn.client.DictFeignClient;
import com.xxxx.yygh.hosp.repository.HospitalRepository;
import com.xxxx.yygh.hosp.service.HospitalService;
import com.xxxx.yygh.model.hosp.Hospital;
import com.xxxx.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    //保存医院信息
    @Override
    public void save(Map<String, Object> paramMap) {

        String mapString = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        //判断是否存在相同的数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);


        if(hospitalExist != null){
            hospitalExist.setStatus(hospitalExist.getStatus());
            hospitalExist.setCreateTime(hospitalExist.getCreateTime());
            hospitalExist.setUpdateTime(new Date());
            hospitalExist.setIsDeleted(0);
            hospitalRepository.save(hospitalExist);
        }else {
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    @Override
    public Page selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        PageRequest pageable = PageRequest.of(page - 1, limit);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<Hospital> example = Example.of(hospital, exampleMatcher);

        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        pages.stream().forEach( item ->{
            this.setHospitalHospType(item);
        });
        return pages;
    }

    @Override
    public void updateHospStatus(String id, Integer status) {
        //根据id查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();
        //设置修改的值
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    @Override
    public Map<String,Object> getHospById(String id) {

        Map<String, Object> result = new HashMap<>();
        Hospital hospital = this.setHospitalHospType(hospitalRepository.findById(id).get());
        //医院基本信息
        result.put("hospital",hospital);
        //vuhe
        result.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        return result;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital !=null){
            return hospital.getHosname();
        }
        return null;
    }
    //根据医院名称查询
    @Override
    public List<Hospital> findByHosname(String hosname) {

        return hospitalRepository.findHospByHosnameLike(hosname);
    }

    @Override
    public Map<String, Object> getBookingRulesPage(String hoscode) {
        Hospital hospital = this.setHospitalHospType(this.getByHoscode(hoscode));
        Map<String, Object> result = new HashMap<>();
        result.put("hospital",hospital);
        result.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        return result;
    }

    private Hospital setHospitalHospType(Hospital hospital) {
        //根据dictCode和value获取医院等级名称
        String hostypeString = dictFeignClient.getName("Hostype", hospital.getHostype());
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districString = dictFeignClient.getName(hospital.getDistrictCode());

        hospital.getParam().put("fullAddress",provinceString+cityString+districString);
        hospital.getParam().put("hosptypeString",hostypeString);
        return hospital;
    }
}
