package com.mysawit.harvest.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HarvestRequestTest {

    @Test
    void gettersAndSettersWork() {
        HarvestRequest request = new HarvestRequest();
        LocalDateTime harvestDate = LocalDateTime.of(2026, 2, 1, 9, 30);

        request.setPlantationId(1L);
        request.setHarvestDate(harvestDate);
        request.setWeight(123.45);
        request.setQuality("PREMIUM");
        request.setHarvesterId(2L);
        request.setNotes("notes");

        assertEquals(1L, request.getPlantationId());
        assertEquals(harvestDate, request.getHarvestDate());
        assertEquals(123.45, request.getWeight());
        assertEquals("PREMIUM", request.getQuality());
        assertEquals(2L, request.getHarvesterId());
        assertEquals("notes", request.getNotes());
    }
}
