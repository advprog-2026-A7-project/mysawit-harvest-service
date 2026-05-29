package com.mysawit.harvest.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PlantationAssignmentEvent {
    private String eventId;

    @JsonAlias({"userId", "mandorId", "supirId", "workerId"})
    private String userId;

    private String name;
    private String role;
    private String plantationId;
    private String action;
    private OffsetDateTime occurredAt;
}
