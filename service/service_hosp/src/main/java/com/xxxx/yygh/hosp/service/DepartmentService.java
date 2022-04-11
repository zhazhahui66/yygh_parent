package com.xxxx.yygh.hosp.service;

import com.xxxx.yygh.model.hosp.Department;
import com.xxxx.yygh.vo.hosp.DepartmentQueryVo;
import com.xxxx.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> paramMap);

    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> findDeptTree(String hoscode);

    String getDepName(String hoscode, String depcode);

    List<Department> getDepartmentsPage(String hoscode);

    Department getDepartment(String hoscode, String depcode);
}
