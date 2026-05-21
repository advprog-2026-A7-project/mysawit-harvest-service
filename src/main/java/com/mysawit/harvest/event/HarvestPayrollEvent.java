package com.mysawit.harvest.event;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class HarvestPayrollEvent {
    private String eventId;      
    private UUID harvestId;
    private UUID harvesterId;
    private UUID foremanId;     
    private String plantationId; 
    private Double weight;       
    private String status;
    private OffsetDateTime occurredAt; 
}
