package com.mysawit.harvest.config;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
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
}