package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.ForemanViewHarvestRequest;
import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.dto.HarvesterViewHarvestRequest;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.repository.HarvestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceImplTest {
    @Mock
    private HarvestRepository harvestRepository;

    @InjectMocks
    private HarvestServiceImpl harvestService;

    private UUID harvesterId;
    private UUID foremanId;
    private UUID plantationId;
    private String harvesterName;

    private LogHarvestRequest logRequest;
    private HarvesterViewHarvestRequest harvesterViewRequest;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        plantationId = UUID.randomUUID();
        harvesterName = "Strawberry Shortcake";

        logRequest = new LogHarvestRequest();
        logRequest.setPlantationId(plantationId);
        logRequest.setWeight(777.0);
        logRequest.setNews("Successful harvest");

        harvesterViewRequest = new HarvesterViewHarvestRequest();
    }

    // HARVEST LOG ------------------------------------------------------------------
    @Test
    void logHarvest_Success() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);
        when(harvestRepository.save(any(Harvest.class))).thenReturn(
                Harvest.builder()
                        .id(UUID.randomUUID())
                        .harvesterId(harvesterId)
                        .foremanId(foremanId)
                        .harvesterName(harvesterName)
                        .plantationId(plantationId)
                        .weight(777.0)
                        .news("Successful harvest")
                        .harvestDate(LocalDateTime.now())
                        .status(HarvestStatus.PENDING)
                        .build()
        );

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId, foremanId, harvesterName);

        assertNotNull(response);
        assertEquals(harvesterId, response.getHarvesterId());
        assertEquals(HarvestStatus.PENDING, response.getStatus());
        verify(harvestRepository).save(any(Harvest.class));
    }

    @Test
    void logHarvest_AlreadyLoggedToday_ThrowsException() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(true);

        assertThrows(AlreadyLoggedHarvestTodayException.class, () ->
                harvestService.logHarvest(logRequest, harvesterId, foremanId, harvesterName));

        verify(harvestRepository, never()).save(any());
    }

    // HARVESTER VIEW ------------------------------------------------------------------
    @Test
    void harvesterViewHarvest_FilterByHarvesterId() {
        when(harvestRepository.findAllByHarvesterIdAndDate(eq(harvesterId), any(), any()))
                .thenReturn(List.of(
                        Harvest.builder().harvesterId(harvesterId).build()
                ));

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId, null);

        assertEquals(1, responses.size());
        assertEquals(harvesterId, responses.getFirst().getHarvesterId());

        verify(harvestRepository).findAllByHarvesterIdAndDate(eq(harvesterId), any(), any());
    }

    @Test
    void harvesterViewHarvest_EnsureCorrectMapping() {
        Harvest mockHarvest = Harvest.builder()
                .id(UUID.randomUUID())
                .harvesterId(harvesterId)
                .harvesterName("Strawberry Shortcake")
                .weight(777.0)
                .status(HarvestStatus.APPROVED)
                .news("Harvest from blok A")
                .build();

        when(harvestRepository.findAllByHarvesterIdAndDate(any(), any(), any()))
                .thenReturn(List.of(mockHarvest));

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId, null);

        HarvestResponse result = responses.getFirst();
        assertEquals(777.0, result.getWeight());
        assertEquals(HarvestStatus.APPROVED, result.getStatus());
        assertEquals("Harvest from blok A", result.getNews());
        assertEquals("Strawberry Shortcake", result.getHarvesterName());
    }

    @Test
    void harvesterViewHarvest_ReturnEmptyList() {
        when(harvestRepository.findAllByHarvesterIdAndDate(any(), any(), any()))
                .thenReturn(List.of());

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId, null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void harvesterViewHarvest_AsForeman() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.harvesterViewHarvest(harvesterViewRequest, null, foremanId)
        );

        verify(harvestRepository, never()).findAllByHarvesterIdAndDate(any(), any(), any());
    }

    @Test
    void harvesterViewHarvest_NoIdentity() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.harvesterViewHarvest(harvesterViewRequest, null, null)
        );

        verify(harvestRepository, never()).findAllByHarvesterIdAndDate(any(), any(), any());
    }

    // FOREMAN VIEW ------------------------------------------------------------------
    @Test
    void foremanViewHarvest_Success() {
        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();
        req.setHarvesterName("Strawberry Shortcake");
        req.setStartDate(LocalDateTime.now().minusDays(7));
        req.setEndDate(LocalDateTime.now());

        when(harvestRepository.findAllByHarvesterNameAndDate(
                eq(foremanId), any(), any(), any()))
                .thenReturn(List.of(new Harvest(), new Harvest()));

        List<HarvestResponse> responses = harvestService.foremanViewHarvest(req, null, foremanId);

        assertEquals(2, responses.size());
        verify(harvestRepository).findAllByHarvesterNameAndDate(
                eq(foremanId), eq("Strawberry Shortcake"), any(), any());
    }

    @Test
    void foremanViewHarvest_AsHarvester_ThrowsException() {
        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () ->
                harvestService.foremanViewHarvest(req, harvesterId, null));

        assertEquals("Only registered foremen are permitted to access.", exception.getMessage());
        verify(harvestRepository, never()).findAllByHarvesterNameAndDate(any(), any(), any(), any());
    }

    @Test
    void foremanViewHarvest_NoIdentity_ThrowsException() {
        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () ->
                harvestService.foremanViewHarvest(req, null, null));

        assertEquals("Required identity to view harvest logs.", exception.getMessage());
        verify(harvestRepository, never()).findAllByHarvesterNameAndDate(any(), any(), any(), any());
    }
}