package com.xxxx.yygh.hosp.receiver;

import com.rabbitmq.client.Channel;
import com.xxxx.yygh.common.constant.MqConst;
import com.xxxx.yygh.hosp.service.ScheduleService;
import com.xxxx.yygh.model.hosp.Schedule;
import com.xxxx.yygh.service.RabbitService;
import com.xxxx.yygh.vo.hosp.ScheduleOrderVo;
import com.xxxx.yygh.vo.msm.MsmVo;
import com.xxxx.yygh.vo.order.OrderMqVo;
import org.apache.xmlbeans.impl.xb.xmlconfig.Nsconfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.QUEUE_ORDER}
    ))
    public void receiver(HttpSession session, OrderMqVo orderMqVo, Message message, Channel channel){
        //下单成功后更新预约数
        Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
        schedule.setReservedNumber(orderMqVo.getReservedNumber());
        schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        scheduleService.update(schedule);
        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo){
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,msmVo);
        }

    }
}
