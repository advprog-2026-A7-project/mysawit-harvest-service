package com.mysawit.harvest.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class PayrollPayload {
    private String eventId;      
    private UUID harvestId;
    private UUID harvesterId;
    private UUID foremanId;     
    private String plantationId; 
    private Double weight;       
    private String status;
    private OffsetDateTime occurredAt; 
}
