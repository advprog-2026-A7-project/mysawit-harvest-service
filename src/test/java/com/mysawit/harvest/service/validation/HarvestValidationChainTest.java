package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.HarvestRepository;
import com.mysawit.harvest.repository.UserReplicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HarvestValidationChainTest {
    private HarvestRepository harvestRepository;
    private UserReplicaRepository userReplicaRepository;

    private HarvestValidationChain chain;

    private LogHarvestRequest request;
    private UUID harvesterId;
    private UUID foremanId;

    @BeforeEach
    void setUp() {
        harvestRepository = mock(HarvestRepository.class);
        userReplicaRepository = mock(UserReplicaRepository.class);

        AlreadyLoggedTodayHandler alreadyLoggedTodayHandler = new AlreadyLoggedTodayHandler(harvestRepository);
        HarvesterAssignedHandler harvesterAssignedHandler = new HarvesterAssignedHandler(userReplicaRepository);
        HarvestDataHandler harvestDataHandler = new HarvestDataHandler();

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
    void validate_stopsBeforeAssignedCheck_whenAlreadyLoggedToday() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException.class,
                () -> chain.validate(request, harvesterId)
        );

        verifyNoInteractions(userReplicaRepository);
    }

    @Test
    void validate_stopsBeforeDataCheck_whenHarvesterNotAssigned() {
        UserReplica replicaWithoutMandor = com.mysawit.harvest.model.UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(null)
                .build();

        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);
        when(userReplicaRepository.findById(harvesterId)).thenReturn(java.util.Optional.of(replicaWithoutMandor));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.mysawit.harvest.exception.UnauthorizedUserException.class,
                () -> chain.validate(request, harvesterId)
        );
    }

    @Test
    void validate_throwsException_whenDataIsInvalid() {
        request.setPhotos(List.of());

        UserReplica replica = com.mysawit.harvest.model.UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);
        when(userReplicaRepository.findById(harvesterId)).thenReturn(java.util.Optional.of(replica));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> chain.validate(request, harvesterId)
        );
    }

    @Test
    void validate_success_whenAllHandlersPass() {
        UserReplica replicaWithMandor = com.mysawit.harvest.model.UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);

        when(userReplicaRepository.findById(harvesterId)).thenReturn(java.util.Optional.of(replicaWithMandor));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                chain.validate(request, harvesterId)
        );

        verify(harvestRepository).existsHarvestByHarvesterIdAndHarvestDateBetween(eq(harvesterId), any(), any());
        verify(userReplicaRepository).findById(harvesterId);
    }
}
