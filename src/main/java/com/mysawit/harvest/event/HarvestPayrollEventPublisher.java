package com.mysawit.harvest.event;

import com.mysawit.harvest.config.RabbitMQConfig;
import com.mysawit.harvest.mapper.PayrollMapper;
import com.mysawit.harvest.model.Harvest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HarvestPayrollEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final PayrollMapper payrollMapper;

    public void publishApprovedHarvest(Harvest harvest) {
        HarvestPayrollEvent payload = payrollMapper.mapToPayload(harvest);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYROLL_QUEUE,
                payload
        );
        
        rabbitTemplate.convertAndSend(
                "harvest.exchange",
                "harvest.approved", 
                payload
        );
    }
}
