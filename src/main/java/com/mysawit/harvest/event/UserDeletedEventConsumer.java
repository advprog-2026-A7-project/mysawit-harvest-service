package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDeletedEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(UserDeletedEventConsumer.class);

    private final UserReplicaService userReplicaService;

    @RabbitListener(queues = "${harvest.rabbitmq.queues.user-deleted:harvest.user.deleted.queue}")
    public void onUserDeleted(UserDeletedEvent event) {
        log.info("Received user.deleted userId={} role={}", event.getUserId(), event.getRole());
        userReplicaService.deleteUser(event);
    }
}
