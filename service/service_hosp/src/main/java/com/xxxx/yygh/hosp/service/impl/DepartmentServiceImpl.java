package com.xxxx.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.yygh.hosp.repository.DepartmentRepository;
import com.xxxx.yygh.hosp.service.DepartmentService;
import com.xxxx.yygh.model.hosp.Department;
import com.xxxx.yygh.vo.hosp.DepartmentQueryVo;
import com.xxxx.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    //查询科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //用于封装数据
        List<DepartmentVo> result = new ArrayList<>();
        //根据医院查编号，查询医院所有科室信息
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentQuery);
        //所有科室列表
        List<Department> departmentList = departmentRepository.findAll(example);
        //根据大科室编号 bigcode 分组，获取每个大科室里面下级子科室
        Map<String, List<Department>> departmentMap = departmentList.stream()
                .collect(Collectors.groupingBy(Department::getBigcode));

        //遍历map集合 departmentMap
        for(Map.Entry<String,List<Department>> entry :departmentMap.entrySet()){
            //大科室编号
            String bigcode = entry.getKey();
            //大科室编号对应的数据
            List<Department> departments = entry.getValue();

            //封装大科室
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(bigcode);
            departmentVo.setDepname(departments.get(0).getBigname());

            //封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department : departments) {
                DepartmentVo departmentVo1 = new DepartmentVo();
                departmentVo1.setDepname(department.getDepname());
                departmentVo1.setDepcode(department.getDepcode());
                //封装到list集合
                children.add(departmentVo1);
            }
            //把小科室list集合放到大科室children里面
            departmentVo.setChildren(children);
            //放到result
            result.add(departmentVo);
        }
        return result;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department != null) {
            return department.getDepname();
        }
        return  null;
    }

    @Override
    public List<Department> getDepartmentsPage(String hoscode) {

        return departmentRepository.getDepartmentByHoscode(hoscode);
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {

        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);
    }

}
