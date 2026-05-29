package com.mysawit.harvest.event;

import com.mysawit.harvest.service.UserReplicaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MandorPlantationAssignedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MandorPlantationAssignedEventConsumer.class);

    private final UserReplicaService userReplicaService;

    @RabbitListener(queues = "${harvest.rabbitmq.queues.mandor-plantation:harvest.mandor.plantation.assigned.queue}")
    public void onMandorPlantationAssigned(Object event) {
        String role = readRole(event);
        if (!"MANDOR".equalsIgnoreCase(role)) {
            return;
        }

        String userId = readUserId(event);
        String actionStr = readAction(event);
        String plantationId = readPlantationId(event);

        log.info("Received plantation.mandor.assignment mandorId={} action={}", userId, actionStr);

        MandorPlantationAssignedEvent.AssignmentAction action =
                "UNASSIGNED".equalsIgnoreCase(actionStr) ?
                        MandorPlantationAssignedEvent.AssignmentAction.UNASSIGNED :
                        MandorPlantationAssignedEvent.AssignmentAction.ASSIGNED;

        MandorPlantationAssignedEvent parsedEvent = new MandorPlantationAssignedEvent(
                userId, plantationId, action, readOccurredAt(event)
        );

        userReplicaService.applyMandorPlantationAssignment(parsedEvent);
    }

    private String readRole(Object event) {
        if (event instanceof MandorPlantationAssignedEvent) {
            return "MANDOR";
        }
        if (event instanceof PlantationAssignmentEvent assignmentEvent) {
            return assignmentEvent.getRole();
        }
        return readMapString(event, "role");
    }

    private String readUserId(Object event) {
        if (event instanceof MandorPlantationAssignedEvent mandorEvent) {
            return mandorEvent.getMandorId();
        }
        if (event instanceof PlantationAssignmentEvent assignmentEvent) {
            return assignmentEvent.getUserId();
        }
        return readMapString(event, "userId");
    }

    private String readAction(Object event) {
        if (event instanceof MandorPlantationAssignedEvent mandorEvent) {
            return mandorEvent.getAction() == null ? null : mandorEvent.getAction().name();
        }
        if (event instanceof PlantationAssignmentEvent assignmentEvent) {
            return assignmentEvent.getAction();
        }
        return readMapString(event, "action");
    }

    private String readPlantationId(Object event) {
        if (event instanceof MandorPlantationAssignedEvent mandorEvent) {
            return mandorEvent.getPlantationId();
        }
        if (event instanceof PlantationAssignmentEvent assignmentEvent) {
            return assignmentEvent.getPlantationId();
        }
        return readMapString(event, "plantationId");
    }

    private Instant readOccurredAt(Object event) {
        if (event instanceof MandorPlantationAssignedEvent mandorEvent && mandorEvent.getOccurredAt() != null) {
            return mandorEvent.getOccurredAt();
        }
        if (event instanceof PlantationAssignmentEvent assignmentEvent && assignmentEvent.getOccurredAt() != null) {
            return assignmentEvent.getOccurredAt().toInstant();
        }
        Object value = readMapValue(event, "occurredAt");
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof String raw && !raw.isBlank()) {
            try {
                return OffsetDateTime.parse(raw).toInstant();
            } catch (RuntimeException ignored) {
                return Instant.now();
            }
        }
        return Instant.now();
    }

    private String readMapString(Object event, String key) {
        Object value = readMapValue(event, key);
        return value == null ? null : value.toString();
    }

    private Object readMapValue(Object event, String key) {
        if (event instanceof Map<?, ?> map) {
            return map.get(key);
        }
        return null;
    }
}
