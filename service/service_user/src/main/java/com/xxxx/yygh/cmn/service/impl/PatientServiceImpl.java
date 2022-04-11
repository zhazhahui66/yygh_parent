package com.xxxx.yygh.cmn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.yygh.cmn.client.DictFeignClient;
import com.xxxx.yygh.model.user.Patient;
import com.xxxx.yygh.cmn.mapper.PatientMapper;
import com.xxxx.yygh.cmn.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper,Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    //获取所有就诊人信息
    @Override
    public List<Patient> findAllByUserId(Long userId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("user_id",userId);
        List<Patient> patientList = baseMapper.selectList(wrapper);
        patientList.stream().forEach(patient ->{
            this.packPatient(patient);
        });


        return patientList;
    }

    @Override
    public Patient getPatientById(Long id) {

        return this.packPatient(baseMapper.selectById(id));
    }

    private Patient  packPatient(Patient patient) {
        //根据证件类型编码，获取证件类型具体指
        //联系人证件
        String certTypeString = dictFeignClient.getName("CertificatesType", patient.getCertificatesType());
        //联系人证件类型
        String contCertTypeString = dictFeignClient.getName("CertificatesType", patient.getContactsCertificatesType());


        String provinceString = dictFeignClient.getName(patient.getProvinceCode());
        String cityString = dictFeignClient.getName(patient.getCityCode());
        String districtString = dictFeignClient.getName(patient.getDistrictCode());

        patient.getParam().put("certTypeString",certTypeString);
        patient.getParam().put("contCertTypeString",contCertTypeString);
        patient.getParam().put("cityString",cityString);
        patient.getParam().put("districtString",districtString);
        patient.getParam().put("provinceString",provinceString);
        patient.getParam().put("fullAddress",provinceString + cityString + districtString +patient.getAddress());

        return patient;
    }
}
