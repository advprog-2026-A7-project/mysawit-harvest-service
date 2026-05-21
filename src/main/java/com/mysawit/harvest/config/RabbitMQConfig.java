package com.mysawit.harvest.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    public static final String PAYROLL_QUEUE = "payroll_queue";

    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";
    public static final String USER_ASSIGNED_ROUTING_KEY = "user.assignment.*";
    public static final String USER_DELETED_ROUTING_KEY = "user.deleted";
    public static final String HARVEST_USER_REGISTERED_QUEUE = "harvest.user.registered.queue";
    public static final String HARVEST_USER_ASSIGNED_QUEUE = "harvest.user.assigned.queue";
    public static final String HARVEST_USER_DELETED_QUEUE = "harvest.user.deleted.queue";

    public static final String HARVEST_EXCHANGE = "harvest.exchange";

    @Bean
    public TopicExchange harvestExchange() {
        return new TopicExchange(HARVEST_EXCHANGE, true, false);
    }

    @Bean
    public Queue payrollQueue() {
        return new Queue(PAYROLL_QUEUE, true);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public Queue harvestUserRegisteredQueue() {
        return new Queue(HARVEST_USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public Binding harvestUserRegisteredBinding(Queue harvestUserRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(harvestUserRegisteredQueue).to(userExchange).with(USER_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public Queue harvestUserAssignedQueue() {
        return new Queue(HARVEST_USER_ASSIGNED_QUEUE, true);
    }

    @Bean
    public Binding harvestUserAssignedBinding(Queue harvestUserAssignedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(harvestUserAssignedQueue).to(userExchange).with(USER_ASSIGNED_ROUTING_KEY);
    }

    @Bean
    public Queue harvestUserDeletedQueue() {
        return new Queue(HARVEST_USER_DELETED_QUEUE, true);
    }

    @Bean
    public Binding harvestUserDeletedBinding(Queue harvestUserDeletedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(harvestUserDeletedQueue).to(userExchange).with(USER_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        org.springframework.amqp.support.converter.DefaultClassMapper classMapper = new org.springframework.amqp.support.converter.DefaultClassMapper();
        classMapper.setTrustedPackages("*");
        
        java.util.Map<String, Class<?>> idClassMapping = new java.util.HashMap<>();
        idClassMapping.put("com.mysawit.identity.event.UserAssignedEvent", com.mysawit.harvest.event.UserAssignedEvent.class);
        idClassMapping.put("com.mysawit.identity.event.UserRegisteredEvent", com.mysawit.harvest.event.UserRegisteredEvent.class);
        idClassMapping.put("com.mysawit.identity.event.UserDeletedEvent", com.mysawit.harvest.event.UserDeletedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        
        converter.setClassMapper(classMapper);
        return converter;
    }
}
