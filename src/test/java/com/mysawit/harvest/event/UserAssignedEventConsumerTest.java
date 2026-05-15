package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserAssignedEventConsumerTest {

    private UserReplicaService userReplicaService;
    private UserAssignedEventConsumer consumer;

    @BeforeEach
    void setUp() {
        userReplicaService = mock(UserReplicaService.class);
        consumer = new UserAssignedEventConsumer(userReplicaService);
    }

    @Test
    void onUserAssigned_delegatesToReplicaService() {
        UserAssignedEvent event = new UserAssignedEvent(
                "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-0000000000aa",
                "Pak Mandor",
                UserAssignedEvent.AssignmentAction.ASSIGNED,
                Instant.now());

        consumer.onUserAssigned(event);

        verify(userReplicaService).applyAssignment(event);
    }
}
