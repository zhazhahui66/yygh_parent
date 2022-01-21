package com.xxxx.yygh.hosp.service;

import com.xxxx.yygh.model.hosp.Department;
import com.xxxx.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> paramMap);

    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void remove(String hoscode, String depcode);
}
