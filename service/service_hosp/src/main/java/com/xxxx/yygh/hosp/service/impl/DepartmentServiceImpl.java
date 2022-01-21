package com.xxxx.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.yygh.hosp.repository.DepartmentRepository;
import com.xxxx.yygh.hosp.service.DepartmentService;
import com.xxxx.yygh.model.hosp.Department;
import com.xxxx.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> paramMap) {
        String paramMapString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(paramMapString,Department.class);

        //根据医院编号和科室编号查询
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        //判断修改还是新增
        if(departmentExist!=null){

            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else{
            department.setUpdateTime(new Date());
            department.setCreateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);

        //创建PageaBle对象，设置当前页和每页记录数
        PageRequest pageable = PageRequest.of(page - 1, limit);
        //创建Example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<Department> example = Example.of(department, matcher);

        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        //根据医院编号和科室编号查询
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);

        if(department !=null){
            departmentRepository.deleteById(department.getId());
        }
    }

}
