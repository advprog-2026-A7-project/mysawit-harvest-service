package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.repository.HarvestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestLogServiceImplTest {
    @Mock
    private HarvestRepository harvestRepository;

    @InjectMocks
    private HarvestLogServiceImpl harvestLogService;

    private UUID harvesterId;
    private UUID foremanId;
    private UUID plantationId;
    private HarvestRequest validRequest;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        plantationId = UUID.randomUUID();

        validRequest = new HarvestRequest();
        validRequest.setPlantationId(plantationId);
        validRequest.setWeight(300.5);
        validRequest.setNews("Successful harvest");
    }

    @Test
    void logSuccess() {
        when(harvestRepository.existsTodaysHarvestByHarvesterId(
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

        HarvestResponse response = harvestLogService.logHarvest(validRequest, harvesterId, foremanId);

        assertNotNull(response);
        assertEquals(harvesterId, response.getHarvesterId());
        assertEquals(foremanId, response.getForemanId());
        assertEquals(300.5, response.getWeight());
        assertEquals(HarvestStatus.PENDING, response.getStatus());
        verify(harvestRepository, times(1)).save(any(Harvest.class));
    }

    @Test
    void alreadyLoggedHarvestToday() {
        when(harvestRepository.existsTodaysHarvestByHarvesterId(
                eq(harvesterId), any(), any()
        )).thenReturn(true);

        assertThrows(AlreadyLoggedHarvestTodayException.class, () ->
                harvestLogService.logHarvest(validRequest, harvesterId, foremanId)
        );

        verify(harvestRepository, never()).save(any(Harvest.class));
    }

}