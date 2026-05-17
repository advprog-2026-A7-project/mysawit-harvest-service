package com.mysawit.harvest.mapper;

import com.mysawit.harvest.dto.PayrollPayload;
import com.mysawit.harvest.model.Harvest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class PayrollMapper {
    public PayrollPayload mapToPayload(Harvest harvest) {
        return PayrollPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .harvestId(harvest.getId())
                .harvesterId(harvest.getHarvesterId())
                .foremanId(harvest.getForemanId())
                .plantationId(String.valueOf(harvest.getPlantationId()))
                .weight(harvest.getWeight())
                .status(harvest.getStatus().name())
                .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }
}