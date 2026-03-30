package com.mysawit.harvest.dto;

import com.mysawit.harvest.model.HarvestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HarvestResponse {
    private UUID id;
    private UUID plantationId;
    private UUID harvesterId;
    private UUID foremanId;
    private Double weight;
    private String news;
    private List<String> photos;
    private HarvestStatus status;
    private String rejectionReason;
    private LocalDateTime harvestDate;
    private LocalDateTime statusUpdatedDate;
}
