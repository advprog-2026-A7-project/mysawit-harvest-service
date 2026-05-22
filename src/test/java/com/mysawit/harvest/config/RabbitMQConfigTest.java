package com.mysawit.harvest.config;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = RabbitMQConfig.class)
class RabbitMQConfigTest {
    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @Test
    void testPayrollQueueBeanExists() {
        assertTrue(context.containsBean("payrollQueue"), "Bean payrollQueue harus terdaftar");
        Queue queue = context.getBean("payrollQueue", Queue.class);
        assertEquals(RabbitMQConfig.PAYROLL_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void testMessageConverterBeanExists() {
        assertTrue(context.containsBean("jsonMessageConverter"), "Bean jsonMessageConverter harus ada");
        MessageConverter converter = context.getBean(MessageConverter.class);
        assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
    }

    @Test
    void testUserExchangeBeanExists() {
        TopicExchange exchange = context.getBean("userExchange", TopicExchange.class);
        assertEquals(RabbitMQConfig.USER_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
    }

    @Test
    void testUserRegisteredQueueAndBinding() {
        Queue queue = context.getBean("harvestUserRegisteredQueue", Queue.class);
        assertEquals(RabbitMQConfig.HARVEST_USER_REGISTERED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());

        Binding binding = context.getBean("harvestUserRegisteredBinding", Binding.class);
        assertEquals(RabbitMQConfig.HARVEST_USER_REGISTERED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.USER_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.USER_REGISTERED_ROUTING_KEY, binding.getRoutingKey());
    }

    @Test
    void testUserAssignedQueueAndBinding() {
        Queue queue = context.getBean("harvestUserAssignedQueue", Queue.class);
        assertEquals(RabbitMQConfig.HARVEST_USER_ASSIGNED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());

        Binding binding = context.getBean("harvestUserAssignedBinding", Binding.class);
        assertEquals(RabbitMQConfig.HARVEST_USER_ASSIGNED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.USER_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.USER_ASSIGNED_ROUTING_KEY, binding.getRoutingKey());
    }
}
