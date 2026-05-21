package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MandorPlantationAssignedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MandorPlantationAssignedEventConsumer.class);

    private final UserReplicaService userReplicaService;

    @RabbitListener(queues = "${harvest.rabbitmq.queues.mandor-plantation:harvest.mandor.plantation.assigned.queue}")
    public void onMandorPlantationAssigned(java.util.Map<String, Object> event) {
        String role = (String) event.get("role");
        if (!"MANDOR".equalsIgnoreCase(role)) {
            return;
        }

        String userId = (String) event.get("userId");
        String actionStr = (String) event.get("action");
        String plantationId = (String) event.get("plantationId");

        log.info("Received plantation.mandor.assignment mandorId={} action={}", userId, actionStr);

        MandorPlantationAssignedEvent.AssignmentAction action =
                "UNASSIGNED".equalsIgnoreCase(actionStr) ?
                        MandorPlantationAssignedEvent.AssignmentAction.UNASSIGNED :
                        MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED;

        MandorPlantationAssignedEvent parsedEvent = new MandorPlantationAssignedEvent(
                userId, plantationId, action, java.time.Instant.now()
        );

        userReplicaService.applyMandorPlantationAssignment(parsedEvent);
    }
}
