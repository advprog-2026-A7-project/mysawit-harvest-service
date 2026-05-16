package com.mysawit.harvest.adapter;

import com.mysawit.harvest.config.RabbitMQConfig;
import com.mysawit.harvest.mapper.PayrollMapper;
import com.mysawit.harvest.model.Harvest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQPayrollAdapter implements PayrollAdapter {
    private final RabbitTemplate rabbitTemplate;
    private final PayrollMapper payrollMapper;

    @Override
    public void publishApprovedHarvest(Harvest harvest) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYROLL_QUEUE,
                payrollMapper.mapToPayload(harvest)
        );
    }
}
