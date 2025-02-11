package com.qiu.trade.listener;

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
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue",durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"

    ))
    public void listenPaySuccess(Long orderId){
        //查询订单
        Order order = orderService.getById(orderId);
        //判断订单状态是否为未支付
        if(order == null || order.getStatus() != 1){
            return;
        }
        //标记订单已支付
        orderService.markOrderPaySuccess(orderId);
    }

}
