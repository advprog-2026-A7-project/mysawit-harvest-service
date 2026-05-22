package com.mysawit.harvest;

import com.mysawit.harvest.controller.HarvestController;
import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.service.HarvestService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HarvestCoverageTest {

    @Test
    void requestAndEntityAccessorsRoundTripValues() {
        LocalDateTime harvestedAt = LocalDateTime.of(2026, 5, 22, 7, 0);
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(11L);
        request.setHarvestDate(harvestedAt);
        request.setWeight(500.5);
        request.setQuality("PREMIUM");
        request.setHarvesterId(21L);
        request.setNotes("Morning harvest");

        assertEquals(11L, request.getPlantationId());
        assertEquals(harvestedAt, request.getHarvestDate());
        assertEquals(500.5, request.getWeight());
        assertEquals("PREMIUM", request.getQuality());
        assertEquals(21L, request.getHarvesterId());
        assertEquals("Morning harvest", request.getNotes());

        Harvest harvest = new Harvest();
        harvest.setId(1L);
        harvest.setPlantationId(request.getPlantationId());
        harvest.setHarvestDate(request.getHarvestDate());
        harvest.setWeight(request.getWeight());
        harvest.setQuality(request.getQuality());
        harvest.setHarvesterId(request.getHarvesterId());
        harvest.setNotes(request.getNotes());
        harvest.setCreatedAt(harvestedAt);
        harvest.setUpdatedAt(harvestedAt.plusHours(1));

        assertEquals(1L, harvest.getId());
        assertEquals(11L, harvest.getPlantationId());
        assertEquals(harvestedAt, harvest.getHarvestDate());
        assertEquals(500.5, harvest.getWeight());
        assertEquals("PREMIUM", harvest.getQuality());
        assertEquals(21L, harvest.getHarvesterId());
        assertEquals("Morning harvest", harvest.getNotes());
        assertEquals(harvestedAt, harvest.getCreatedAt());
        assertEquals(harvestedAt.plusHours(1), harvest.getUpdatedAt());
    }

    @Test
    void createEndpointMapsServiceRuntimeExceptionToBadRequest() {
        HarvestService harvestService = mock(HarvestService.class);
        HarvestRequest request = new HarvestRequest();
        when(harvestService.createHarvest(request)).thenThrow(new RuntimeException("invalid harvest"));

        HarvestController controller = new HarvestController(harvestService);
        ResponseEntity<?> response = controller.createHarvest(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
