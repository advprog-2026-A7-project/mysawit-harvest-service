package com.mysawit.harvest.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MandorPlantationAssignedEvent {
    private String mandorId;
    private String plantationId;
    private AssignmentAction action;
    private Instant occurredAt;

    public enum AssignmentAction {
        ASSIGNED,
        UNASSIGNED,
        REASSIGNED
    }
}
