package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventConsumer.class);

    private final UserReplicaService userReplicaService;

    @RabbitListener(queues = "${harvest.rabbitmq.queues.user-registered:harvest.user.registered.queue}")
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received user.registered userId={}", event.getUserId());
        userReplicaService.upsertFromRegistration(event);
    }
}
