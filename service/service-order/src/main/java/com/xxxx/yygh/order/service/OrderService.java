package com.xxxx.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.yygh.model.order.OrderInfo;
import com.xxxx.yygh.vo.order.OrderQueryVo;
import org.junit.jupiter.api.Order;

public interface OrderService extends IService<OrderInfo> {
    /**
     * 创建订单
     * @param scheduleId 排班id
     * @param patientId 就诊人id
     * @return 订单编号
     */
    public Long saveOrder(String scheduleId, Long patientId);

    /**
     * 分页列表
     */
    IPage<OrderInfo> selectPage(Page<OrderInfo> page, OrderQueryVo orderQueryVo);
}
