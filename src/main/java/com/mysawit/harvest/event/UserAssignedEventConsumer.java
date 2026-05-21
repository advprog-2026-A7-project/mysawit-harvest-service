package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAssignedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserAssignedEventConsumer.class);

    private final UserReplicaService userReplicaService;

    @RabbitListener(queues = "${harvest.rabbitmq.queues.user-assigned:harvest.user.assigned.queue}")
    public void onUserAssigned(UserAssignedEvent event) {
        log.info("Received user.assigned userId={} action={}", event.getUserId(), event.getAction());
        userReplicaService.applyAssignment(event);
    }
}
