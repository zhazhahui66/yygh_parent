package com.xxxx.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.yygh.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {
    List<Patient> findAllByUserId(Long userId);

    Patient getPatientById(Long id);
}
