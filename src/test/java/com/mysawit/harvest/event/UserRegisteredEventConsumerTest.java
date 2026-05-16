package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserRegisteredEventConsumerTest {

    private UserReplicaService userReplicaService;
    private UserRegisteredEventConsumer consumer;

    @BeforeEach
    void setUp() {
        userReplicaService = mock(UserReplicaService.class);
        consumer = new UserRegisteredEventConsumer(userReplicaService);
    }

    @Test
    void onUserRegistered_delegatesToReplicaService() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "00000000-0000-0000-0000-000000000001", "buruh@mail.com", "BURUH", "budi");

        consumer.onUserRegistered(event);

        verify(userReplicaService).upsertFromRegistration(event);
    }
}
