package com.xxxx.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.common.result.ResultCodeEnum;
import com.xxxx.common.result.exception.YyghException;
import com.xxxx.yygh.hosp.mapper.ScheduleMapper;
import com.xxxx.yygh.hosp.repository.ScheduleRepository;
import com.xxxx.yygh.hosp.service.DepartmentService;
import com.xxxx.yygh.hosp.service.HospitalService;
import com.xxxx.yygh.hosp.service.ScheduleService;
import com.xxxx.yygh.model.hosp.BookingRule;
import com.xxxx.yygh.model.hosp.Department;
import com.xxxx.yygh.model.hosp.Hospital;
import com.xxxx.yygh.model.hosp.Schedule;
import com.xxxx.yygh.vo.hosp.BookingScheduleRuleVo;
import com.xxxx.yygh.vo.hosp.ScheduleOrderVo;
import com.xxxx.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper,Schedule>  implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> paramMap) {
        String mapString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(mapString, Schedule.class);

        Schedule scheduleExists  = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if(scheduleExists !=null){
            scheduleExists.setUpdateTime(new Date());
            scheduleExists.setIsDeleted(0);
            scheduleExists.setStatus(1);
            scheduleRepository.save(scheduleExists);
        }else {
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }

    }

    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);

        //???????????????
        PageRequest pageable = PageRequest.of(page - 1, limit);
        //????????????
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnoreCase(true)
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        //example??????
        Example<Schedule> example = Example.of(schedule, exampleMatcher);
        //??????
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {

        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(schedule !=null){
            scheduleRepository.deleteById(schedule.getId());
        }
    }
    //??????????????????&???????????????????????????????????????
    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
        //???????????????????????????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //???????????????workDate???????????????
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//????????????
                Aggregation.group("workDate")//????????????
                        .first("workDate").as("workDate")

                //??????????????????
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                //????????????
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        //???????????????????????????
        AggregationResults<BookingScheduleRuleVo> aggResult = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResult.getMappedResults();

        //???????????????????????????
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResult = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        int total = totalAggResult.getMappedResults().size();

        //???????????????????????????
        for(BookingScheduleRuleVo bookingScheduleRuleVo:bookingScheduleRuleVoList){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //??????????????????
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleVoList",bookingScheduleRuleVoList);
        result.put("total",total);

        //??????????????????
        String hosName = hospitalService.getHospName(hoscode);

        Map<String, String> baseMap =  new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);
        return result;
    }

    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        //??????????????????mongdb
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        //?????????list?????????????????????????????????????????????????????????????????????????????????
        scheduleList.stream().forEach(item ->{
            this.packageSchedule(item);
        });

        return scheduleList;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();

        //??????????????????
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if(null == hospital){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //?????????????????????????????????
        IPage iPage = this.getListDate(page,limit,bookingRule);

        //????????????????????????
        List<Date> dateList = iPage.getRecords();

        //???????????????????????????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleList = aggregationResults.getMappedResults();

        //???????????????????????????

        //???????????? ???????????????ScheduleVo ??????"????????????" ?????????BookingRuleVo
        Map<Date,BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleList)){
            scheduleVoMap = scheduleList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,BookingScheduleRuleVo ->BookingScheduleRuleVo));

            //???????????????????????????
            List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
            for (int i = 0,len = dateList.size(); i < len ; i++) {
                Date date = dateList.get(i);
                BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
                if(null == bookingScheduleRuleVo){ //??????????????????????????????
                    bookingScheduleRuleVo = new BookingScheduleRuleVo();
                    //??????????????????
                    bookingScheduleRuleVo.setDocCount(0);
                    //?????????????????????  -1 ????????????
                    bookingScheduleRuleVo.setAvailableNumber(-1);
                }
                bookingScheduleRuleVo.setWorkDate(date);
                bookingScheduleRuleVo.setWorkDateMd(date);
                //?????????????????????????????????
                String dayOfWeek = this.getDayOfWeek(new DateTime(date));
                bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

                //????????????????????????????????????????????? ??????  0????????? ,1: ????????????, -1????????????????????????
                if(i == len -1 && page == iPage.getPages()){
                    bookingScheduleRuleVo.setStatus(1);
                }else {
                    bookingScheduleRuleVo.setStatus(0);
                }
                //??????????????????????????????????????? ????????????
                if(i ==0 && page ==1){
                    DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                    if(stopTime.isBeforeNow()){
                        //????????????
                        bookingScheduleRuleVo.setStatus(-1);
                    }
                }
                bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
            }
            //???????????????????????????
            result.put("bookingScheduleList",bookingScheduleRuleVoList);
            result.put("total",iPage.getTotal());
            //????????????
            Map<String,String> baseMap = new HashMap<>();
            //????????????
            baseMap.put("hosname",hospitalService.getHospName(hoscode));
            Department department = departmentService.getDepartment(hoscode,depcode);
            baseMap.put("bigname",department.getBigname());
            baseMap.put("depname",department.getDepname());
            baseMap.put("workDateString",new DateTime().toString("yyyy???MM???"));
            baseMap.put("releaseTime",bookingRule.getReleaseTime());
            baseMap.put("stopTime",bookingRule.getStopTime());
            result.put("baseMap",baseMap);

            return result;
        }



        return null;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        //?????????list????????????????????????????????????????????????????????????????????????????????????
        scheduleList.stream().forEach(item->{
            this.packageSchedule(item);
        });
        return scheduleList;
    }

    @Override
    public Schedule getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        this.packageSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //??????????????????
        Schedule schedule = baseMapper.selectById(scheduleId);
        if(schedule == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //????????????????????????
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(hospital == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(bookingRule == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //????????? ?????????scheduleOrderVo
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());


        //?????????????????????????????????????????????-1????????????0???
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //??????????????????
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //??????????????????
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //????????????????????????
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        return scheduleOrderVo;
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());

        //????????????????????????
        scheduleRepository.save(schedule);
    }

    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //??????????????????
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //????????????
        int cycle = bookingRule.getCycle();
        //??????????????????????????????????????????????????????????????????????????????????????????+1
        if(releaseTime.isBeforeNow()){
            cycle +=1;
        }
        //???????????????????????????????????????????????????????????????
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //????????????????????????
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //?????????????????????????????????????????????????????????????????????7????????????????????????????????????
        List<Date> pageDateList = new ArrayList<>();
        int start =(page-1)*limit;
        int end = (page-1)*limit+limit;
        if(end > dateList.size()) end = dateList.size();
        for (int i = start;i<end;i++){
            pageDateList.add(dateList.get(i));
        }
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,7,dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd")+ " " +timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;

    }


    //?????????????????????????????????   ????????????????????????????????????????????????
    private void packageSchedule(Schedule item) {

        item.getParam().put("hosname",hospitalService.getHospName(item.getHoscode()));

        item.getParam().put("depname",departmentService.getDepName(item.getHoscode(),item.getDepcode()));

        item.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(item.getWorkDate())));
    }

    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "??????";
            default:
                break;
        }
        return dayOfWeek;
    }
}
