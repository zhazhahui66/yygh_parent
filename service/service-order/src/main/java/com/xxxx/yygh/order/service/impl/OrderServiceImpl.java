package com.xxxx.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.common.result.ResultCodeEnum;
import com.xxxx.common.result.exception.YyghException;
import com.xxxx.yygh.common.constant.MqConst;
import com.xxxx.yygh.common.helper.HttpRequestHelper;
import com.xxxx.yygh.enums.OrderStatusEnum;
import com.xxxx.yygh.hosp.client.HospitalFeignClient;
import com.xxxx.yygh.model.order.OrderInfo;
import com.xxxx.yygh.model.user.Patient;
import com.xxxx.yygh.order.mapper.OrderInfoMapper;
import com.xxxx.yygh.order.service.OrderService;
import com.xxxx.yygh.service.RabbitService;
import com.xxxx.yygh.user.client.PatientFeignClient;
import com.xxxx.yygh.vo.hosp.ScheduleOrderVo;
import com.xxxx.yygh.vo.msm.MsmVo;
import com.xxxx.yygh.vo.order.OrderCountQueryVo;
import com.xxxx.yygh.vo.order.OrderMqVo;
import com.xxxx.yygh.vo.order.OrderQueryVo;
import com.xxxx.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    //生成挂号订单
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        Patient patient = patientFeignClient.getPatientOrder(patientId);
        if(patient ==null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getsScheduleOrderVo(scheduleId);
        if(scheduleOrderVo ==null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //当前时间不可预约
        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()){
            throw  new YyghException(ResultCodeEnum.TIME_NO);
        }
        //获取密钥签名
        SignInfoVo signInfo = hospitalFeignClient.getSignInfo(scheduleOrderVo.getHoscode());
        if(null == signInfo){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //判断可预约挂号数
        if(scheduleOrderVo.getAvailableNumber() <=0){
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);

        String uuid = IdWorker.get32UUID();
        orderInfo.setOutTradeNo(uuid);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);


        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfo.getSignKey());
        paramMap.put("sign",sign);
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfo.getApiUrl() + "/order/submitOrder");

        if(result.getInteger("code")==200){
            JSONObject data = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = data.getString("hosRecordId");
            //预约序号
            Integer number = data.getInteger("number");;
            //取号时间
            String fetchTime = data.getString("fetchTime");;
            //取号地址
            String fetchAddress = data.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchAddress(fetchAddress);
            orderInfo.setFetchTime(fetchTime);
            baseMapper.updateById(orderInfo);

            //排班可预约数
            Integer reservedNumber = data.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = data.getInteger("availableNumber");

            //发送mq 信息更新号源和短信通知
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);

            //短信通知
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());

            msmVo.setTemplateCode("SMS_194640721");
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);

            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

        }else {
            throw new YyghException(result.getString("message"),ResultCodeEnum.FAIL.getCode());

        }

        return orderInfo.getId();
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> page, OrderQueryVo orderQueryVo) {
        //orderQueryVo 获取条件值
        //医院名称
        String name = orderQueryVo.getKeyword();
        Long patientId = orderQueryVo.getPatientId();
        String orderStatus = orderQueryVo.getOrderStatus();
        String reserveDate = orderQueryVo.getReserveDate();

        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("hosname",name);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }

        //调用mapper方法
        IPage<OrderInfo> pages = baseMapper.selectPage(page, wrapper);

        //编号变成对应值封装
        pages.getRecords().stream().forEach(item ->{
            this.packOrderInfo(item);
        });

        return pages;
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
