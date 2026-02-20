package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.service.HarvestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HarvestControllerTest {

    private HarvestService harvestService;
    private HarvestController harvestController;

    @BeforeEach
    void setUp() {
        harvestService = mock(HarvestService.class);
        harvestController = new HarvestController(harvestService);
    }

    @Test
    void getAllHarvestsUsesPlantationFilter() {
        Harvest harvest = sampleHarvest(1L);
        when(harvestService.getHarvestsByPlantationId(10L)).thenReturn(List.of(harvest));

        ResponseEntity<List<Harvest>> response = harvestController.getAllHarvests(10L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(harvestService).getHarvestsByPlantationId(10L);
        verify(harvestService, never()).getHarvestsByHarvesterId(anyLong());
        verify(harvestService, never()).getAllHarvests();
    }

    @Test
    void getAllHarvestsUsesHarvesterFilter() {
        Harvest harvest = sampleHarvest(2L);
        when(harvestService.getHarvestsByHarvesterId(99L)).thenReturn(List.of(harvest));

        ResponseEntity<List<Harvest>> response = harvestController.getAllHarvests(null, 99L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(harvestService).getHarvestsByHarvesterId(99L);
        verify(harvestService, never()).getAllHarvests();
    }

    @Test
    void getAllHarvestsReturnsAllWhenNoFilter() {
        when(harvestService.getAllHarvests()).thenReturn(List.of(sampleHarvest(1L), sampleHarvest(2L)));

        ResponseEntity<List<Harvest>> response = harvestController.getAllHarvests(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(harvestService).getAllHarvests();
    }

    @Test
    void getHarvestByIdReturnsHarvest() {
        Harvest harvest = sampleHarvest(1L);
        when(harvestService.getHarvestById(1L)).thenReturn(harvest);

        ResponseEntity<?> response = harvestController.getHarvestById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(harvest, response.getBody());
    }

    @Test
    void getHarvestByIdReturnsNotFoundWhenMissing() {
        when(harvestService.getHarvestById(1L)).thenThrow(new RuntimeException("missing"));

        ResponseEntity<?> response = harvestController.getHarvestById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void createHarvestReturnsCreated() {
        Harvest harvest = sampleHarvest(1L);
        HarvestRequest request = sampleRequest();
        when(harvestService.createHarvest(request)).thenReturn(harvest);

        ResponseEntity<?> response = harvestController.createHarvest(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(harvest, response.getBody());
    }

    @Test
    void createHarvestReturnsBadRequestOnError() {
        HarvestRequest request = sampleRequest();
        when(harvestService.createHarvest(request)).thenThrow(new RuntimeException("invalid"));

        ResponseEntity<?> response = harvestController.createHarvest(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void updateHarvestReturnsUpdated() {
        Harvest harvest = sampleHarvest(1L);
        HarvestRequest request = sampleRequest();
        when(harvestService.updateHarvest(1L, request)).thenReturn(harvest);

        ResponseEntity<?> response = harvestController.updateHarvest(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(harvest, response.getBody());
    }

    @Test
    void updateHarvestReturnsNotFoundOnError() {
        HarvestRequest request = sampleRequest();
        when(harvestService.updateHarvest(1L, request)).thenThrow(new RuntimeException("missing"));

        ResponseEntity<?> response = harvestController.updateHarvest(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void deleteHarvestReturnsSuccessMessage() {
        ResponseEntity<?> response = harvestController.deleteHarvest(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Harvest deleted successfully", ((Map<?, ?>) response.getBody()).get("message"));
        verify(harvestService).deleteHarvest(1L);
    }

    @Test
    void deleteHarvestReturnsNotFoundOnError() {
        doThrow(new RuntimeException("missing")).when(harvestService).deleteHarvest(1L);

        ResponseEntity<?> response = harvestController.deleteHarvest(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void healthReturnsUpStatus() {
        ResponseEntity<Map<String, String>> response = harvestController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("mysawit-harvest-service", response.getBody().get("service"));
    }

    private Harvest sampleHarvest(Long id) {
        Harvest harvest = new Harvest();
        harvest.setId(id);
        harvest.setPlantationId(10L);
        harvest.setHarvestDate(LocalDateTime.now());
        harvest.setWeight(100.0);
        harvest.setQuality("STANDARD");
        harvest.setHarvesterId(99L);
        harvest.setNotes("note");
        return harvest;
    }

    private HarvestRequest sampleRequest() {
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(10L);
        request.setHarvestDate(LocalDateTime.now());
        request.setWeight(100.0);
        request.setQuality("PREMIUM");
        request.setHarvesterId(99L);
        request.setNotes("note");
        return request;
    }
}
