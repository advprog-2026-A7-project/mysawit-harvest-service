package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvesterContext;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HarvesterContextServiceTest {
    private UserReplicaRepository userReplicaRepository;
    private HarvesterContextService service;
    private UUID harvesterId;
    private UUID mandorId;

    @BeforeEach
    void setUp() {
        userReplicaRepository = mock(UserReplicaRepository.class);
        service = new HarvesterContextService(userReplicaRepository);
        harvesterId = UUID.randomUUID();
        mandorId = UUID.randomUUID();
    }

    @Test
    void resolve_ValidHarvester_ReturnsContext() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name("Strawberry")
                .role("BURUH")
                .mandorId(mandorId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        HarvesterContext context = service.resolve(harvesterId);

        assertNotNull(context);
        assertEquals("Strawberry", context.harvesterName());
        assertEquals(mandorId, context.foremanId());
    }

    @Test
    void resolve_ReplicaNotFound_ThrowsUnauthorizedUserException() {
        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.empty());

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> {
            service.resolve(harvesterId);
        });

        assertEquals("Harvester registration data not found.", exception.getMessage());
    }

    @Test
    void resolve_WrongRole_ThrowsUnauthorizedUserException() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name("Strawberry")
                .role("MANDOR")
                .mandorId(mandorId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> {
            service.resolve(harvesterId);
        });

        assertEquals("User is not a harvester.", exception.getMessage());
    }

    @Test
    void resolve_MandorIdNull_ThrowsUnauthorizedUserException() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name("Strawberry")
                .role("BURUH")
                .mandorId(null)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> {
            service.resolve(harvesterId);
        });

        assertEquals("Harvester is not assigned to any foreman.", exception.getMessage());
    }
}