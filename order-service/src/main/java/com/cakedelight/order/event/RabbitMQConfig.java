package com.cakedelight.order.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order.exchange";

    public static final String COMPLETED_QUEUE = "order.completed.queue";
    public static final String COMPLETED_ROUTING_KEY = "order.completed";

    public static final String STATUS_QUEUE = "order.status.changed.queue";
    public static final String STATUS_ROUTING_KEY = "order.status.changed";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderCompletedQueue() {
        return new Queue(COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding orderCompletedBinding(
            Queue orderCompletedQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(orderCompletedQueue)
                .to(orderExchange)
                .with(COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Queue orderStatusChangedQueue() {
        return new Queue(STATUS_QUEUE, true);
    }

    @Bean
    public Binding orderStatusChangedBinding(
            Queue orderStatusChangedQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(orderStatusChangedQueue)
                .to(orderExchange)
                .with(STATUS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}