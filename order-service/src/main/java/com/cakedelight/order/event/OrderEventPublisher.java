package com.cakedelight.order.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Order completed event
    public void publishOrderCompleted(OrderCompletedEvent event) {
        rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.completed",
                event
        );
    }

    // Order status changed event
    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.status.changed",
                event
        );
    }
}