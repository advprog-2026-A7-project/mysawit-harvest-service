package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.client.IdentityClient;
import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.repository.HarvestRepository;
import com.mysawit.harvest.repository.UserReplicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HarvestValidationChainTest {

    private HarvestRepository harvestRepository;
    private UserReplicaRepository userReplicaRepository;
    private IdentityClient identityClient;

    private HarvestValidationChain chain;

    private LogHarvestRequest request;
    private UUID harvesterId;
    private UUID foremanId;

    @BeforeEach
    void setUp() {
        harvestRepository = mock(HarvestRepository.class);
        userReplicaRepository = mock(UserReplicaRepository.class);
        identityClient = mock(IdentityClient.class);

        AlreadyLoggedTodayHandler alreadyLoggedTodayHandler =
                new AlreadyLoggedTodayHandler(harvestRepository);
        HarvesterAssignedHandler harvesterAssignedHandler =
                new HarvesterAssignedHandler(userReplicaRepository, identityClient);
        HarvestDataHandler harvestDataHandler =
                new HarvestDataHandler();

        chain = new HarvestValidationChain(
                alreadyLoggedTodayHandler,
                harvesterAssignedHandler,
                harvestDataHandler
        );

        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();

        request = new LogHarvestRequest();
        request.setWeight(777.0);
        request.setNews("Panen bagus");
        request.setPhotos(List.of("photo.jpg"));
    }

    @Test
    void validate_passes_whenAllHandlersPass() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);
        when(userReplicaRepository.findById(harvesterId)).thenReturn(java.util.Optional.empty());
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        assertDoesNotThrow(() -> chain.validate(request, harvesterId));

        verify(harvestRepository).existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any());
        verify(identityClient).getAssignedForemanId(harvesterId);
    }

    @Test
    void validate_stopsBeforeAssignedCheck_whenAlreadyLoggedToday() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException.class,
                () -> chain.validate(request, harvesterId)
        );

        verifyNoInteractions(userReplicaRepository);
        verifyNoInteractions(identityClient);
    }

    @Test
    void validate_stopsBeforeDataCheck_whenHarvesterNotAssigned() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);
        when(userReplicaRepository.findById(harvesterId)).thenReturn(java.util.Optional.empty());
        when(identityClient.getAssignedForemanId(harvesterId))
                .thenThrow(new com.mysawit.harvest.exception.UnauthorizedUserException(
                        "Harvester is not assigned to any foreman."
                ));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.mysawit.harvest.exception.UnauthorizedUserException.class,
                () -> chain.validate(request, harvesterId)
        );
    }

    @Test
    void validate_throwsException_whenDataIsInvalid() {
        request.setPhotos(List.of());

        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);
        when(userReplicaRepository.findById(harvesterId)).thenReturn(java.util.Optional.empty());
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> chain.validate(request, harvesterId)
        );
    }
}
