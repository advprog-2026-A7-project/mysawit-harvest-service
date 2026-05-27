package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MandorPlantationAssignedEventConsumerTest {
    @Mock
    private UserReplicaService userReplicaService;

    @InjectMocks
    private MandorPlantationAssignedEventConsumer eventConsumer;

    private String foremanId;
    private String driverId;
    private String plantationId;

    @BeforeEach
    void setUp() {
        foremanId = UUID.randomUUID().toString();
        driverId = UUID.randomUUID().toString();
        plantationId = UUID.randomUUID().toString();
    }

    @Test
    void onMandorPlantationAssigned_Success_AssignedAction() {
        Map<String, Object> event = new HashMap<>();
        event.put("role", "MANDOR");
        event.put("userId", foremanId);
        event.put("action", "ASSIGNED");
        event.put("plantationId", "1");

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor = ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());

        MandorPlantationAssignedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);

        assertEquals(MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED, capturedEvent.getAction());
        assertEquals("1", capturedEvent.getPlantationId());
    }

    @Test
    void onMandorPlantationAssigned_Success_FromTypedPlantationAssignmentEvent() {
        PlantationAssignmentEvent event = new PlantationAssignmentEvent();
        event.setRole("MANDOR");
        event.setUserId(foremanId);
        event.setAction("ASSIGNED");
        event.setPlantationId("2");
        event.setOccurredAt(OffsetDateTime.parse("2026-05-25T12:00:00+07:00"));

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());
        assertEquals("2", eventCaptor.getValue().getPlantationId());
        assertEquals(foremanId, eventCaptor.getValue().getMandorId());
    }

    @Test
    void onMandorPlantationAssigned_Success_FromLegacyMandorEvent() {
        Instant occurredAt = Instant.parse("2026-05-25T05:00:00Z");
        MandorPlantationAssignedEvent event = new MandorPlantationAssignedEvent(
                foremanId,
                "3",
                MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED,
                occurredAt
        );

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());
        assertEquals("3", eventCaptor.getValue().getPlantationId());
        assertEquals(occurredAt, eventCaptor.getValue().getOccurredAt());
    }

    @Test
    void onMandorPlantationAssigned_UsesInstantOccurredAtFromMap() {
        Instant occurredAt = Instant.parse("2026-05-25T06:00:00Z");
        Map<String, Object> event = new HashMap<>();
        event.put("role", "MANDOR");
        event.put("userId", foremanId);
        event.put("action", "ASSIGNED");
        event.put("plantationId", "4");
        event.put("occurredAt", occurredAt);

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());
        assertEquals(occurredAt, eventCaptor.getValue().getOccurredAt());
    }

    @Test
    void onMandorPlantationAssigned_UsesOffsetDateTimeOccurredAtFromMap() {
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-05-25T13:00:00+07:00");
        Map<String, Object> event = new HashMap<>();
        event.put("role", "MANDOR");
        event.put("userId", foremanId);
        event.put("action", "ASSIGNED");
        event.put("plantationId", "5");
        event.put("occurredAt", occurredAt);

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());
        assertEquals(occurredAt.toInstant(), eventCaptor.getValue().getOccurredAt());
    }

    @Test
    void onMandorPlantationAssigned_ParsesStringOccurredAtFromMap() {
        Map<String, Object> event = new HashMap<>();
        event.put("role", "MANDOR");
        event.put("userId", foremanId);
        event.put("action", "ASSIGNED");
        event.put("plantationId", "6");
        event.put("occurredAt", "2026-05-25T14:00:00+07:00");

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());
        assertEquals(OffsetDateTime.parse("2026-05-25T14:00:00+07:00").toInstant(), eventCaptor.getValue().getOccurredAt());
    }

    @Test
    void onMandorPlantationAssigned_FallsBackWhenStringOccurredAtInvalid() {
        Map<String, Object> event = new HashMap<>();
        event.put("role", "MANDOR");
        event.put("userId", foremanId);
        event.put("action", "ASSIGNED");
        event.put("plantationId", "7");
        event.put("occurredAt", "not-a-date");

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());
        assertNotNull(eventCaptor.getValue().getOccurredAt());
    }

    @Test
    void onMandorPlantationAssigned_Success_UnassignedAction() {
        Map<String, Object> event = new HashMap<>();
        event.put("role", "mandor");
        event.put("userId", foremanId);
        event.put("action", "UNASSIGNED");
        event.put("plantationId", plantationId);

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());

        MandorPlantationAssignedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(MandorPlantationAssignedEvent.AssignmentAction.UNASSIGNED, capturedEvent.getAction());
    }

    @Test
    void onMandorPlantationAssigned_Success_DefaultToAssigned_WhenActionIsRandom() {
        Map<String, Object> event = new HashMap<>();
        event.put("role", "MANDOR");
        event.put("userId", "user-123");
        event.put("action", "ANY_OTHER_STRING");
        event.put("plantationId", plantationId);

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor =
                ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());
        assertEquals(MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED, eventCaptor.getValue().getAction());
    }

    @Test
    void onMandorPlantationAssigned_Ignored_WhenRoleIsNotMandor() {
        Map<String, Object> event = new HashMap<>();
        event.put("role", "SUPIR");
        event.put("userId", driverId);
        event.put("action", "ASSIGNED");
        event.put("plantationId", plantationId);

        eventConsumer.onMandorPlantationAssigned(event);

        verify(userReplicaService, never()).applyMandorPlantationAssignment(any());
    }

    @Test
    void onMandorPlantationAssigned_Ignored_WhenPayloadIsUnknownObject() {
        eventConsumer.onMandorPlantationAssigned(new Object());

        verify(userReplicaService, never()).applyMandorPlantationAssignment(any());
    }
}
