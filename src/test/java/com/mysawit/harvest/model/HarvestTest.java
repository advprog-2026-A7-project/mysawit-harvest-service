package com.mysawit.harvest.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HarvestTest {
    @Test
    void defaultStatusIsPending() {
        Harvest harvest = new Harvest();

        assertEquals(HarvestStatus.PENDING, harvest.getStatus());
    }

    @Test
    void buildHarvest() {
        Harvest harvest = Harvest.builder()
                .harvesterId(java.util.UUID.randomUUID())
                .foremanId(java.util.UUID.randomUUID())
                .plantationId("PLT-12345678")
                .weight(777.0)
                .news("Successful harvest")
                .build();

        assertEquals(300.5, harvest.getWeight());
        assertEquals("Successful harvest", harvest.getNews());
        assertEquals(HarvestStatus.PENDING, harvest.getStatus());
        assertNull(harvest.getRejectionReason());
        assertNull(harvest.getStatusUpdatedDate());
    }
}