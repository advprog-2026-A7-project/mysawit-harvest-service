package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeletedEventConsumerTest {
    @Mock
    private UserReplicaService userReplicaService;

    @InjectMocks
    private UserDeletedEventConsumer userDeletedEventConsumer;

    private UserDeletedEvent sampleEvent;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        sampleEvent = new UserDeletedEvent();
        sampleEvent.setUserId(String.valueOf(userId));
        sampleEvent.setRole("BURUH");
    }

    @Test
    void onUserDeleted_shouldLogAndCallServiceToDeleteUser() {
        userDeletedEventConsumer.onUserDeleted(sampleEvent);

        verify(userReplicaService, times(1)).deleteUser(sampleEvent);
    }
}