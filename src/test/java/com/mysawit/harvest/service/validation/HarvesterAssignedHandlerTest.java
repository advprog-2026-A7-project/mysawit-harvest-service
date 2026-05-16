package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.client.IdentityClient;
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
    private IdentityClient identityClient;
    private HarvesterAssignedHandler handler;
    private UUID harvesterId;
    private UUID foremanId;

    @BeforeEach
    void setUp() {
        userReplicaRepository = mock(UserReplicaRepository.class);
        identityClient = mock(IdentityClient.class);
        handler = new HarvesterAssignedHandler(userReplicaRepository, identityClient);
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
    }

    @Test
    void handle_passesFromReplica_whenReplicaIsAssignedHarvester() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        assertDoesNotThrow(() -> handler.handle(new LogHarvestRequest(), harvesterId));
        verifyNoInteractions(identityClient);
    }

    @Test
    void handle_fallsBackToIdentity_whenReplicaMissing() {
        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.empty());
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        assertDoesNotThrow(() -> handler.handle(new LogHarvestRequest(), harvesterId));
        verify(identityClient).getAssignedForemanId(harvesterId);
    }

    @Test
    void handle_throwsException_whenIdentitySaysNotAssigned() {
        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.empty());
        when(identityClient.getAssignedForemanId(harvesterId))
                .thenThrow(new UnauthorizedUserException("Harvester is not assigned to any foreman."));

        assertThrows(UnauthorizedUserException.class,
                () -> handler.handle(new LogHarvestRequest(), harvesterId));
    }

    @Test
    void handle_passesWithoutCallingIdentity_whenReplicaIsAssignedHarvester() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        assertDoesNotThrow(() -> handler.handle(new LogHarvestRequest(), harvesterId));

        verify(userReplicaRepository).findById(harvesterId);
        verifyNoInteractions(identityClient);
    }

    @Test
    void handle_fallsBackToIdentity_whenReplicaHasNoMandorId() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .role("BURUH")
                .mandorId(null)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        assertDoesNotThrow(() -> handler.handle(new LogHarvestRequest(), harvesterId));

        verify(identityClient).getAssignedForemanId(harvesterId);
    }

    @Test
    void handle_fallsBackToIdentity_whenReplicaRoleIsNotHarvester() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .role("MANDOR")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        assertDoesNotThrow(() -> handler.handle(new LogHarvestRequest(), harvesterId));

        verify(identityClient).getAssignedForemanId(harvesterId);
    }
}