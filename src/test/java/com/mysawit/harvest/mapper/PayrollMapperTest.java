package com.mysawit.harvest.mapper;

import com.mysawit.harvest.event.HarvestPayrollEvent;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollMapperTest {

    private PayrollMapper payrollMapper;

    @BeforeEach
    void setUp() {
        payrollMapper = new PayrollMapper();
    }

    @Test
    void mapToPayload_shouldMapHarvestToPayrollPayload() {
        UUID harvestId = UUID.randomUUID();
        UUID harvesterId = UUID.randomUUID();

        Harvest harvest = Harvest.builder()
                .id(harvestId)
                .harvesterId(harvesterId)
                .weight(777.0)
                .status(HarvestStatus.APPROVED)
                .build();

        HarvestPayrollEvent payload = payrollMapper.mapToPayload(harvest);

        assertEquals(harvestId, payload.getHarvestId());
        assertEquals(harvesterId, payload.getHarvesterId());
        assertEquals(777.0, payload.getWeight());
        assertEquals("APPROVED", payload.getStatus());
    }
}
