package com.mysawit.harvest.service;

import com.mysawit.harvest.event.UserAssignedEvent;
import com.mysawit.harvest.event.UserRegisteredEvent;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReplicaServiceTest {

    @Mock
    private UserReplicaRepository repository;

    private UserReplicaService service;

    @BeforeEach
    void setUp() {
        service = new UserReplicaService(repository);
    }

    @Test
    void upsertFromRegistration_createsNewReplica_whenAbsent() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId.toString(), "buruh@mail.com", "BURUH", "budi");
        when(repository.findById(userId)).thenReturn(Optional.empty());

        service.upsertFromRegistration(event);

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());
        UserReplica saved = captor.getValue();
        assertEquals(userId, saved.getId());
        assertEquals("budi", saved.getName());
        assertEquals("BURUH", saved.getRole());
        assertNull(saved.getMandorId());
    }

    @Test
    void upsertFromRegistration_preservesExistingMandorId() {
        UUID userId = UUID.randomUUID();
        UUID existingMandor = UUID.randomUUID();
        UserReplica existing = UserReplica.builder()
                .id(userId).name("old").role("BURUH").mandorId(existingMandor).build();
        when(repository.findById(userId)).thenReturn(Optional.of(existing));

        service.upsertFromRegistration(new UserRegisteredEvent(
                userId.toString(), "buruh@mail.com", "BURUH", "budi"));

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());
        assertEquals(existingMandor, captor.getValue().getMandorId());
        assertEquals("budi", captor.getValue().getName());
    }

    @Test
    void upsertFromRegistration_fallsBackToEmail_whenUsernameBlank() {
        UUID userId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        service.upsertFromRegistration(new UserRegisteredEvent(
                userId.toString(), "buruh@mail.com", "BURUH", ""));

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());
        assertEquals("buruh@mail.com", captor.getValue().getName());
    }

    @Test
    void upsertFromRegistration_upsertsAllRoles() {
        for (String role : new String[]{"BURUH", "MANDOR", "ADMIN", "SUPIR"}) {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());
            service.upsertFromRegistration(new UserRegisteredEvent(
                    id.toString(), "x@mail.com", role, "user-" + role));
        }
        verify(repository, times(4)).save(any(UserReplica.class));
    }

    @Test
    void upsertFromRegistration_skipsInvalidUserId() {
        service.upsertFromRegistration(new UserRegisteredEvent(
                "not-a-uuid", "x@mail.com", "BURUH", "x"));
        service.upsertFromRegistration(new UserRegisteredEvent(
                null, "x@mail.com", "BURUH", "x"));
        service.upsertFromRegistration(new UserRegisteredEvent(
                "", "x@mail.com", "BURUH", "x"));

        verifyNoInteractions(repository);
    }

    @Test
    void upsertFromRegistration_fallsBackToEmail_whenUsernameNull() {
        UUID userId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        UserRegisteredEvent event = new UserRegisteredEvent(
                userId.toString(), "buruh@mail.com", "BURUH", null);

        service.upsertFromRegistration(event);

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());
        assertEquals("buruh@mail.com", captor.getValue().getName());
    }

    @Test
    void applyAssignment_setsMandorId_whenAssigned() {
        UUID userId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();
        UserReplica existing = UserReplica.builder()
                .id(userId).name("budi").role("BURUH").build();
        when(repository.findById(userId)).thenReturn(Optional.of(existing));

        service.applyAssignment(new UserAssignedEvent(
                userId.toString(), mandorId.toString(), "Pak Mandor",
                UserAssignedEvent.AssignmentAction.ASSIGNED, Instant.now()));

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());
        assertEquals(mandorId, captor.getValue().getMandorId());
        assertEquals("budi", captor.getValue().getName());
        assertEquals("BURUH", captor.getValue().getRole());
    }

    @Test
    void applyAssignment_clearsMandorId_whenUnassigned() {
        UUID userId = UUID.randomUUID();
        UserReplica existing = UserReplica.builder()
                .id(userId).name("budi").role("BURUH").mandorId(UUID.randomUUID()).build();
        when(repository.findById(userId)).thenReturn(Optional.of(existing));

        service.applyAssignment(new UserAssignedEvent(
                userId.toString(), null, null,
                UserAssignedEvent.AssignmentAction.UNASSIGNED, Instant.now()));

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());
        assertNull(captor.getValue().getMandorId());
    }

    @Test
    void applyAssignment_createsStubReplica_whenAssignedBeforeRegistered() {
        UUID userId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        service.applyAssignment(new UserAssignedEvent(
                userId.toString(), mandorId.toString(), "Pak Mandor",
                UserAssignedEvent.AssignmentAction.ASSIGNED, Instant.now()));

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());
        assertEquals(userId, captor.getValue().getId());
        assertEquals(mandorId, captor.getValue().getMandorId());
        assertNull(captor.getValue().getName());
        assertNull(captor.getValue().getRole());
    }

    @Test
    void applyAssignment_skipsInvalidUserId() {
        service.applyAssignment(new UserAssignedEvent(
                "not-a-uuid", UUID.randomUUID().toString(), "x",
                UserAssignedEvent.AssignmentAction.ASSIGNED, Instant.now()));

        verifyNoInteractions(repository);
    }

    @Test
    void deleteUser_shouldDeleteFromRepository_whenUserExists() {
        UUID userId = UUID.randomUUID();
        com.mysawit.harvest.event.UserDeletedEvent event = new com.mysawit.harvest.event.UserDeletedEvent();
        event.setUserId(userId.toString());
        event.setRole("BURUH");

        when(repository.existsById(userId)).thenReturn(true);

        service.deleteUser(event);

        verify(repository, times(1)).existsById(userId);
        verify(repository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_shouldOnlyLogWarning_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        com.mysawit.harvest.event.UserDeletedEvent event = new com.mysawit.harvest.event.UserDeletedEvent();
        event.setUserId(userId.toString());
        event.setRole("BURUH");

        when(repository.existsById(userId)).thenReturn(false);

        service.deleteUser(event);

        verify(repository, times(1)).existsById(userId);
        verify(repository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteUser_shouldSkipAndReturn_whenUserIdIsInvalid() {
        com.mysawit.harvest.event.UserDeletedEvent event1 = new com.mysawit.harvest.event.UserDeletedEvent();
        event1.setUserId("bukan-format-uuid");
        event1.setRole("BURUH");

        com.mysawit.harvest.event.UserDeletedEvent event2 = new com.mysawit.harvest.event.UserDeletedEvent();
        event2.setUserId(null);
        event2.setRole("BURUH");

        com.mysawit.harvest.event.UserDeletedEvent event3 = new com.mysawit.harvest.event.UserDeletedEvent();
        event3.setUserId("");
        event3.setRole("BURUH");

        service.deleteUser(event1);
        service.deleteUser(event2);
        service.deleteUser(event3);

        verifyNoInteractions(repository);
    }

    @Test
    void applyMandorPlantationAssignment_skipsInvalidMandorId() {
        com.mysawit.harvest.event.MandorPlantationAssignedEvent event =
                new com.mysawit.harvest.event.MandorPlantationAssignedEvent();
        event.setMandorId("bukan-format-uuid");
        event.setPlantationId("PLT-99");
        event.setAction(com.mysawit.harvest.event.MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED);

        service.applyMandorPlantationAssignment(event);

        verifyNoInteractions(repository);
    }

    @Test
    void applyMandorPlantationAssignment_setsPlantationId_whenAssigned() {
        UUID mandorId = UUID.randomUUID();
        String plantationId = "PLT-12345";

        UserReplica existingMandor = UserReplica.builder()
                .id(mandorId).name("Pak Mandor Ahmad").role("MANDOR").build();
        when(repository.findById(mandorId)).thenReturn(Optional.of(existingMandor));

        com.mysawit.harvest.event.MandorPlantationAssignedEvent event =
                new com.mysawit.harvest.event.MandorPlantationAssignedEvent();
        event.setMandorId(mandorId.toString());
        event.setPlantationId(plantationId);
        event.setAction(com.mysawit.harvest.event.MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED);

        service.applyMandorPlantationAssignment(event);

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());

        UserReplica saved = captor.getValue();
        assertEquals(mandorId, saved.getId());
        assertEquals(plantationId, saved.getPlantationId());
        assertEquals("Pak Mandor Ahmad", saved.getName());
    }

    @Test
    void applyMandorPlantationAssignment_clearsPlantationId_whenUnassigned() {
        UUID mandorId = UUID.randomUUID();

        UserReplica existingMandor = UserReplica.builder()
                .id(mandorId).name("Pak Mandor Ahmad").role("MANDOR").plantationId("PLT-OLD").build();
        when(repository.findById(mandorId)).thenReturn(Optional.of(existingMandor));

        com.mysawit.harvest.event.MandorPlantationAssignedEvent event =
                new com.mysawit.harvest.event.MandorPlantationAssignedEvent();
        event.setMandorId(mandorId.toString());
        event.setPlantationId("PLT-OLD");
        event.setAction(com.mysawit.harvest.event.MandorPlantationAssignedEvent.AssignmentAction.UNASSIGNED);

        service.applyMandorPlantationAssignment(event);

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());

        assertNull(captor.getValue().getPlantationId());
    }

    @Test
    void applyMandorPlantationAssignment_createsStubReplica_whenMandorAbsent() {
        UUID mandorId = UUID.randomUUID();
        String plantationId = "PLT-777";

        when(repository.findById(mandorId)).thenReturn(Optional.empty());

        com.mysawit.harvest.event.MandorPlantationAssignedEvent event =
                new com.mysawit.harvest.event.MandorPlantationAssignedEvent();
        event.setMandorId(mandorId.toString());
        event.setPlantationId(plantationId);
        event.setAction(com.mysawit.harvest.event.MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED);

        service.applyMandorPlantationAssignment(event);

        ArgumentCaptor<UserReplica> captor = ArgumentCaptor.forClass(UserReplica.class);
        verify(repository).save(captor.capture());

        UserReplica saved = captor.getValue();
        assertEquals(mandorId, saved.getId());
        assertEquals(plantationId, saved.getPlantationId());
        assertNull(saved.getName());
    }
}
