package com.xxxx.yygh.hosp.repository;

import com.xxxx.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DepartmentRepository extends MongoRepository<Department,String> {
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

    List<Department> getDepartmentByHoscode(String hoscode);
}
