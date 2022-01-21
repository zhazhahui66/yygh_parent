package com.xxxx.yygh.hosp.repository;

import com.xxxx.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);
}
