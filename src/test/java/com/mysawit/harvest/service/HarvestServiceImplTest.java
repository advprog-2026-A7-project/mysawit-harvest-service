package com.mysawit.harvest.service;

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

    private LogHarvestRequest logRequest;
    private HarvesterViewHarvestRequest viewRequest;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        plantationId = UUID.randomUUID();

        logRequest = new LogHarvestRequest();
        logRequest.setPlantationId(plantationId);
        logRequest.setWeight(300.5);
        logRequest.setNews("Successful harvest");

        viewRequest = new HarvesterViewHarvestRequest();
    }

    // HARVEST LOG ------------------------------------------------------------------
    @Test
    void logHarvest_LogSuccess() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any()
        )).thenReturn(false);

        when(harvestRepository.save(any(Harvest.class))).thenReturn(
                Harvest.builder()
                        .id(UUID.randomUUID())
                        .harvesterId(harvesterId)
                        .foremanId(foremanId)
                        .plantationId(plantationId)
                        .weight(300.5)
                        .news("Successful harvest")
                        .status(HarvestStatus.PENDING)
                        .build()
        );

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId, foremanId);

        assertNotNull(response);
        assertEquals(harvesterId, response.getHarvesterId());
        assertEquals(foremanId, response.getForemanId());
        assertEquals(300.5, response.getWeight());
        assertEquals(HarvestStatus.PENDING, response.getStatus());
        verify(harvestRepository, times(1)).save(any(Harvest.class));
    }

    @Test
    void logHarvest_AlreadyLoggedHarvestToday() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any()
        )).thenReturn(true);

        assertThrows(AlreadyLoggedHarvestTodayException.class, () ->
                harvestService.logHarvest(logRequest, harvesterId, foremanId)
        );

        verify(harvestRepository, never()).save(any(Harvest.class));
    }

    // HARVEST VIEW ------------------------------------------------------------------
    @Test
    void viewHarvest_FilterByHarvesterId() {
        when(harvestRepository.findAllByHarvesterIdAndHarvestDateBetween(eq(harvesterId), any(), any()))
                .thenReturn(List.of(
                        Harvest.builder().harvesterId(harvesterId).build()
                ));

        List<HarvestResponse> responses = harvestService.viewHarvest(viewRequest, harvesterId, null);

        assertEquals(1, responses.size());
        assertEquals(harvesterId, responses.getFirst().getHarvesterId());

        verify(harvestRepository).findAllByHarvesterIdAndHarvestDateBetween(eq(harvesterId), any(), any());
    }

    @Test
    void viewHarvest_PassCorrectDateRange() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 7, 10, 0);

        viewRequest.setStartDate(start);
        viewRequest.setEndDate(end);

        LocalDateTime expectedStart = viewRequest.getStartDate();
        LocalDateTime expectedEnd = viewRequest.getEndDate();

        when(harvestRepository.findAllByHarvesterIdAndHarvestDateBetween(any(), eq(expectedStart), eq(expectedEnd)))
                .thenReturn(List.of());

        harvestService.viewHarvest(viewRequest, harvesterId, null);

        verify(harvestRepository).findAllByHarvesterIdAndHarvestDateBetween(any(), eq(expectedStart), eq(expectedEnd));
    }

    @Test
    void viewHarvest_EnsureCorrectMapping() {
        Harvest mockHarvest = Harvest.builder()
                .id(UUID.randomUUID())
                .weight(250.75)
                .status(HarvestStatus.APPROVED)
                .news("Harvest from blok A")
                .build();

        when(harvestRepository.findAllByHarvesterIdAndHarvestDateBetween(any(), any(), any()))
                .thenReturn(List.of(mockHarvest));

        List<HarvestResponse> responses = harvestService.viewHarvest(viewRequest, harvesterId, null);

        HarvestResponse result = responses.getFirst();
        assertEquals(250.75, result.getWeight());
        assertEquals(HarvestStatus.APPROVED, result.getStatus());
        assertEquals("Harvest from blok A", result.getNews());
    }

    @Test
    void viewHarvest_ReturnEmptyList() {
        when(harvestRepository.findAllByHarvesterIdAndHarvestDateBetween(any(), any(), any()))
                .thenReturn(List.of());

        List<HarvestResponse> responses = harvestService.viewHarvest(viewRequest, harvesterId, null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void viewHarvest_AsForeman_ShouldThrowUnauthorizedForNow() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.viewHarvest(viewRequest, null, foremanId)
        );

        verify(harvestRepository, never()).findAllByHarvesterIdAndHarvestDateBetween(any(), any(), any());
    }

    @Test
    void viewHarvest_NoIdentity_ShouldThrowUnauthorized() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.viewHarvest(viewRequest, null, null)
        );

        verify(harvestRepository, never()).findAllByHarvesterIdAndHarvestDateBetween(any(), any(), any());
    }

//    @Test
//    void viewHarvest_Unauthorized() {
//        assertThrows(UnauthorizedUserException.class, () ->
//                harvestService.viewHarvest(viewRequest, null, null)
//        );
//
//        verify(harvestRepository, never()).findAllByHarvesterIdAndHarvestDateBetween(any(), any(), any());
//        verify(harvestRepository, never()).findAllByForemanIdAndHarvestDateBetween(any(), any(), any());
//    }
    // Will use later after foreman-view is done because if not code will always red and not green
}