package com.mysawit.harvest.mapper;

import com.mysawit.harvest.event.HarvestPayrollEvent;
import com.mysawit.harvest.model.Harvest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class PayrollMapper {
    public HarvestPayrollEvent mapToPayload(Harvest harvest) {
        return HarvestPayrollEvent.builder()
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