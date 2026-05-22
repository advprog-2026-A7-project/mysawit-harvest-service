package com.mysawit.harvest.event;

import com.mysawit.harvest.config.RabbitMQConfig;
import com.mysawit.harvest.mapper.PayrollMapper;
import com.mysawit.harvest.model.Harvest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.Mockito.*;

class HarvestPayrollEventPublisherTest {
    private RabbitTemplate rabbitTemplate;
    private PayrollMapper payrollMapper;
    private HarvestPayrollEventPublisher publisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        payrollMapper = mock(PayrollMapper.class);
        publisher = new HarvestPayrollEventPublisher(rabbitTemplate, payrollMapper);
    }

    @Test
    void publishApprovedHarvest_sendsMappedPayloadToPayrollQueue() {
        Harvest harvest = Harvest.builder()
                .id(UUID.randomUUID())
                .harvesterId(UUID.randomUUID())
                .weight(777.0)
                .build();

        HarvestPayrollEvent payload = HarvestPayrollEvent.builder()
                .harvestId(harvest.getId())
                .harvesterId(harvest.getHarvesterId())
                .weight(harvest.getWeight())
                .status("APPROVED")
                .build();

        when(payrollMapper.mapToPayload(harvest)).thenReturn(payload);

        publisher.publishApprovedHarvest(harvest);

        verify(payrollMapper).mapToPayload(harvest);
        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.PAYROLL_QUEUE, payload);
    }
}
