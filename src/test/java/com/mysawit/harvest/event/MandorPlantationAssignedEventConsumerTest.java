package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        event.put("plantationId", plantationId);

        eventConsumer.onMandorPlantationAssigned(event);

        ArgumentCaptor<MandorPlantationAssignedEvent> eventCaptor = ArgumentCaptor.forClass(MandorPlantationAssignedEvent.class);

        verify(userReplicaService, times(1)).applyMandorPlantationAssignment(eventCaptor.capture());

        MandorPlantationAssignedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);

        assertEquals(MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED, capturedEvent.getAction());
        assertEquals(plantationId, capturedEvent.getPlantationId());
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
}