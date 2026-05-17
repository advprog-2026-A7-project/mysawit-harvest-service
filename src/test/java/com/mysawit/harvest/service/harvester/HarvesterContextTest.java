package com.mysawit.harvest.service.harvester;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HarvesterContextTest {

    @Test
    void constructor_shouldStoreHarvesterNameAndForemanId() {
        UUID foremanId = UUID.randomUUID();

        HarvesterContext context = new HarvesterContext("Budi", foremanId);

        assertEquals("Budi", context.harvesterName());
        assertEquals(foremanId, context.foremanId());
    }
}