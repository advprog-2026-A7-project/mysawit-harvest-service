package com.mysawit.harvest.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HarvestTest {

    @Test
    void gettersSettersAndDefaultsWork() {
        Harvest harvest = new Harvest();
        LocalDateTime date = LocalDateTime.of(2026, 3, 1, 8, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 1, 10, 0);

        assertEquals("STANDARD", harvest.getQuality());

        harvest.setId(1L);
        harvest.setPlantationId(2L);
        harvest.setHarvestDate(date);
        harvest.setWeight(200.0);
        harvest.setQuality("LOW");
        harvest.setHarvesterId(3L);
        harvest.setNotes("note");
        harvest.setCreatedAt(createdAt);
        harvest.setUpdatedAt(updatedAt);

        assertEquals(1L, harvest.getId());
        assertEquals(2L, harvest.getPlantationId());
        assertEquals(date, harvest.getHarvestDate());
        assertEquals(200.0, harvest.getWeight());
        assertEquals("LOW", harvest.getQuality());
        assertEquals(3L, harvest.getHarvesterId());
        assertEquals("note", harvest.getNotes());
        assertEquals(createdAt, harvest.getCreatedAt());
        assertEquals(updatedAt, harvest.getUpdatedAt());
    }

    @Test
    void lifecycleHooksSetTimestamps() {
        Harvest harvest = new Harvest();

        harvest.onCreate();

        assertNotNull(harvest.getCreatedAt());
        assertNotNull(harvest.getUpdatedAt());

        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        harvest.setUpdatedAt(beforeUpdate.minusDays(1));

        harvest.onUpdate();

        assertTrue(harvest.getUpdatedAt().isAfter(beforeUpdate));
    }
}
