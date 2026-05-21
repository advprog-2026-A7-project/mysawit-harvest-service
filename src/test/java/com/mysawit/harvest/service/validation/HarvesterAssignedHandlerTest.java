package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HarvesterAssignedHandlerTest {
    private UserReplicaRepository userReplicaRepository;
    private HarvesterAssignedHandler handler;
    private UUID harvesterId;
    private UUID foremanId;
    private LogHarvestRequest request;

    @BeforeEach
    void setUp() {
        userReplicaRepository = mock(UserReplicaRepository.class);
        handler = new HarvesterAssignedHandler(userReplicaRepository);
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        request = new LogHarvestRequest();
    }

    @Test
    void handle_passes_whenReplicaIsAssignedHarvester() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        assertDoesNotThrow(() -> handler.handle(request, harvesterId));
        verify(userReplicaRepository).findById(harvesterId);
    }

    @Test
    void handle_throwsException_whenReplicaNotFound() {
        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.empty());

        UnauthorizedUserException exception = assertThrows(
                UnauthorizedUserException.class,
                () -> handler.handle(request, harvesterId)
        );

        assertEquals("Harvester registration data not found in local replica.", exception.getMessage());
    }

    @Test
    void handle_throwsException_whenRoleIsNotHarvester() {
        UserReplica invalidRoleReplica = UserReplica.builder()
                .id(harvesterId)
                .role("MANDOR")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(invalidRoleReplica));

        UnauthorizedUserException exception = assertThrows(
                UnauthorizedUserException.class,
                () -> handler.handle(request, harvesterId)
        );

        assertEquals("User is not a harvester.", exception.getMessage());
    }

    @Test
    void handle_throwsException_whenMandorIdIsNull() {
        UserReplica replicaWithoutMandor = UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(null)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replicaWithoutMandor));

        UnauthorizedUserException exception = assertThrows(
                UnauthorizedUserException.class,
                () -> handler.handle(request, harvesterId)
        );

        assertEquals("Harvester is not assigned to any foreman.", exception.getMessage());
    }
}