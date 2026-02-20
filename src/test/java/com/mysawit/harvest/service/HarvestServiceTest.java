package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.repository.HarvestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HarvestServiceTest {

    private HarvestRepository harvestRepository;
    private HarvestService harvestService;

    @BeforeEach
    void setUp() {
        harvestRepository = mock(HarvestRepository.class);
        harvestService = new HarvestService(harvestRepository);
    }

    @Test
    void getAllHarvestsReturnsRepositoryData() {
        when(harvestRepository.findAll()).thenReturn(List.of(new Harvest(), new Harvest()));

        List<Harvest> result = harvestService.getAllHarvests();

        assertEquals(2, result.size());
    }

    @Test
    void getHarvestByIdReturnsEntity() {
        Harvest harvest = new Harvest();
        when(harvestRepository.findById(1L)).thenReturn(Optional.of(harvest));

        Harvest result = harvestService.getHarvestById(1L);

        assertSame(harvest, result);
    }

    @Test
    void getHarvestByIdThrowsWhenMissing() {
        when(harvestRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> harvestService.getHarvestById(1L));

        assertEquals("Harvest not found with id: 1", exception.getMessage());
    }

    @Test
    void getHarvestsByPlantationIdReturnsRepositoryData() {
        when(harvestRepository.findByPlantationId(10L)).thenReturn(List.of(new Harvest()));

        assertEquals(1, harvestService.getHarvestsByPlantationId(10L).size());
    }

    @Test
    void getHarvestsByHarvesterIdReturnsRepositoryData() {
        when(harvestRepository.findByHarvesterId(20L)).thenReturn(List.of(new Harvest()));

        assertEquals(1, harvestService.getHarvestsByHarvesterId(20L).size());
    }

    @Test
    void createHarvestUsesProvidedQuality() {
        HarvestRequest request = sampleRequest("PREMIUM");
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(inv -> inv.getArgument(0));

        Harvest result = harvestService.createHarvest(request);

        assertEquals("PREMIUM", result.getQuality());
        assertEquals(10L, result.getPlantationId());
        assertEquals(30L, result.getHarvesterId());
        assertEquals("note", result.getNotes());
    }

    @Test
    void createHarvestUsesDefaultQualityWhenNull() {
        HarvestRequest request = sampleRequest(null);
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(inv -> inv.getArgument(0));

        Harvest result = harvestService.createHarvest(request);

        assertEquals("STANDARD", result.getQuality());
    }

    @Test
    void updateHarvestUsesProvidedQuality() {
        Harvest existing = new Harvest();
        existing.setId(5L);
        when(harvestRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(inv -> inv.getArgument(0));

        Harvest result = harvestService.updateHarvest(5L, sampleRequest("LOW"));

        assertEquals("LOW", result.getQuality());
        assertEquals(10L, result.getPlantationId());
    }

    @Test
    void updateHarvestUsesDefaultQualityWhenNull() {
        Harvest existing = new Harvest();
        existing.setId(5L);
        when(harvestRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(inv -> inv.getArgument(0));

        Harvest result = harvestService.updateHarvest(5L, sampleRequest(null));

        assertEquals("STANDARD", result.getQuality());
    }

    @Test
    void updateHarvestThrowsWhenMissing() {
        when(harvestRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> harvestService.updateHarvest(5L, sampleRequest("LOW")));

        assertEquals("Harvest not found with id: 5", exception.getMessage());
    }

    @Test
    void deleteHarvestDeletesEntityWhenFound() {
        Harvest existing = new Harvest();
        when(harvestRepository.findById(5L)).thenReturn(Optional.of(existing));

        harvestService.deleteHarvest(5L);

        ArgumentCaptor<Harvest> captor = ArgumentCaptor.forClass(Harvest.class);
        verify(harvestRepository).delete(captor.capture());
        assertSame(existing, captor.getValue());
    }

    @Test
    void deleteHarvestThrowsWhenMissing() {
        when(harvestRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> harvestService.deleteHarvest(5L));

        assertEquals("Harvest not found with id: 5", exception.getMessage());
        verify(harvestRepository, never()).delete(any());
    }

    private HarvestRequest sampleRequest(String quality) {
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(10L);
        request.setHarvestDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setWeight(100.0);
        request.setQuality(quality);
        request.setHarvesterId(30L);
        request.setNotes("note");
        return request;
    }
}
