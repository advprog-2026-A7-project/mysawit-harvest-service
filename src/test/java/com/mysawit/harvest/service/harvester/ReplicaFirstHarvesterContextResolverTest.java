package com.mysawit.harvest.service.harvester;

import com.mysawit.harvest.client.IdentityClient;
import com.mysawit.harvest.dto.IdentityUserResponse;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReplicaFirstHarvesterContextResolverTest {

    private UserReplicaRepository userReplicaRepository;
    private IdentityClient identityClient;
    private ReplicaFirstHarvesterContextResolver resolver;

    private UUID harvesterId;
    private UUID foremanId;

    @BeforeEach
    void setUp() {
        userReplicaRepository = mock(UserReplicaRepository.class);
        identityClient = mock(IdentityClient.class);
        resolver = new ReplicaFirstHarvesterContextResolver(userReplicaRepository, identityClient);

        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
    }

    @Test
    void resolve_usesReplica_whenReplicaIsCompleteHarvester() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name("Budi")
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        HarvesterContext context = resolver.resolve(harvesterId);

        assertEquals("Budi", context.harvesterName());
        assertEquals(foremanId, context.foremanId());
        verifyNoInteractions(identityClient);
    }

    @Test
    void resolve_fallsBackToIdentity_whenReplicaMissing() {
        IdentityUserResponse user = new IdentityUserResponse();
        user.setName("Budi");

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.empty());
        when(identityClient.getUserById(harvesterId)).thenReturn(user);
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        HarvesterContext context = resolver.resolve(harvesterId);

        assertEquals("Budi", context.harvesterName());
        assertEquals(foremanId, context.foremanId());
        verify(identityClient).getUserById(harvesterId);
        verify(identityClient).getAssignedForemanId(harvesterId);
    }

    @Test
    void resolve_fallsBackToIdentity_whenReplicaIncomplete() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name("Budi")
                .role("BURUH")
                .mandorId(null)
                .build();

        IdentityUserResponse user = new IdentityUserResponse();
        user.setName("Budi");

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));
        when(identityClient.getUserById(harvesterId)).thenReturn(user);
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        HarvesterContext context = resolver.resolve(harvesterId);

        assertEquals("Budi", context.harvesterName());
        assertEquals(foremanId, context.foremanId());
    }

    @Test
    void resolve_fallsBackToIdentity_whenReplicaRoleIsNotHarvester() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name("Budi")
                .role("MANDOR")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));
        when(identityClient.getUserById(harvesterId)).thenReturn(new IdentityUserResponse());
        when(identityClient.getAssignedForemanId(harvesterId))
                .thenThrow(new UnauthorizedUserException("User is not a harvester."));

        assertThrows(UnauthorizedUserException.class, () -> resolver.resolve(harvesterId));
    }

    @Test
    void resolve_fallsBackToIdentity_whenReplicaNameIsNull() {
        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name(null)
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        IdentityUserResponse user = new IdentityUserResponse();
        user.setName("Budi From Identity");

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));
        when(identityClient.getUserById(harvesterId)).thenReturn(user);
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);

        HarvesterContext context = resolver.resolve(harvesterId);

        assertEquals("Budi From Identity", context.harvesterName());
        assertEquals(foremanId, context.foremanId());
        verify(identityClient).getUserById(harvesterId);
        verify(identityClient).getAssignedForemanId(harvesterId);
    }
}
