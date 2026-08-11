package com.tianji.learning.mq;

import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.tianji.common.constants.MqConstants.Exchange.ORDER_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.ORDER_PAY_KEY;

/**
 * 订单支付成功消息监听，用于创建课表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LessonPayMessageListener {

    private final ILearningLessonService lessonService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "learning.lesson.pay.queue", durable = "true"),
            exchange = @Exchange(name = ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = ORDER_PAY_KEY))
    public void listenOrderPay(OrderBasicDTO order) {
        if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
            log.debug("订单支付，异常消息，信息为空：{}", order);
            return;
        }
        log.debug("处理订单支付消息：{}", order);
        try {
            lessonService.addUserLessons(order);
        } catch (Exception e) {
            // 捕获异常，避免消息无限重投递，由课表唯一键兜底幂等
            log.error("处理订单支付消息失败，order：{}", order, e);
        }
    }
}
