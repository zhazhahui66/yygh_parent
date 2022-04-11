package com.xxxx.yygh.receiver;

import com.rabbitmq.client.Channel;
import com.xxxx.yygh.common.constant.MqConst;
import com.xxxx.yygh.config.service.MsmService;
import com.xxxx.yygh.service.RabbitService;
import com.xxxx.yygh.vo.msm.MsmVo;
import com.xxxx.yygh.vo.order.OrderMqVo;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

@Component
public class MsmReceiver {

    @Autowired
    private MsmService msmService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(HttpSession session,MsmVo msmVo, Message message, Channel channel){
        msmService.send(session, msmVo);
    }
}
