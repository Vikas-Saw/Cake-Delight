package com.cakedelight.order.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCompleted(OrderCompletedEvent event) {

        rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.completed",
                event
        );
    }
}