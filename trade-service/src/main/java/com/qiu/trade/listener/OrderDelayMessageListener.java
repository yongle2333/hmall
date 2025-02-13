package com.qiu.trade.listener;

import com.qiu.api.client.PayClient;
import com.qiu.api.dto.PayOrderDTO;
import com.qiu.trade.constants.MqConstants;
import com.qiu.trade.domain.po.Order;
import com.qiu.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author qiu
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MqConstants.DELAY_EXCHANGE_NAME,delayed = "true"),
            key = MqConstants.DELAY_ORDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId){
        //1.查询订单
        Order order = orderService.getById(orderId);
        //2.检测本地订单状态，是否已经支付
        if(order == null || order.getStatus() != 1){
            //订单不存在或者已支付
            return;
        }
        //3.未支付，查询支付模块的支付流水
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
        //4.再次判断订单支付状态
        if(payOrderDTO != null && payOrderDTO.getStatus() == 3){
            //4.1已支付，标记订单状态为已支付
            orderService.markOrderPaySuccess(orderId);
        }else {
            //4.2未支付，取消订单，回复库存
            orderService.cancelOrder(orderId);
        }


    }
}
