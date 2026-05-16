package com.mysawit.harvest.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PayrollPayload {
    private UUID harvestId;
    private UUID harvesterId;
    private Double weight;
    private String status;
}
