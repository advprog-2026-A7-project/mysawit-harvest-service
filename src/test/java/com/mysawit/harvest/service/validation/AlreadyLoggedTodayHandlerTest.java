package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.repository.HarvestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlreadyLoggedTodayHandlerTest {

    private HarvestRepository harvestRepository;
    private AlreadyLoggedTodayHandler handler;
    private UUID harvesterId;

    @BeforeEach
    void setUp() {
        harvestRepository = mock(HarvestRepository.class);
        handler = new AlreadyLoggedTodayHandler(harvestRepository);
        harvesterId = UUID.randomUUID();
    }

    @Test
    void handle_passes_whenHarvesterHasNotLoggedToday() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);

        assertDoesNotThrow(() -> handler.handle(new LogHarvestRequest(), harvesterId));
    }

    @Test
    void handle_throwsException_whenHarvesterAlreadyLoggedToday() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(true);

        assertThrows(AlreadyLoggedHarvestTodayException.class,
                () -> handler.handle(new LogHarvestRequest(), harvesterId));
    }
}