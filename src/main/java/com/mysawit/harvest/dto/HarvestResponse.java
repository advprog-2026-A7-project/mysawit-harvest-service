package com.mysawit.harvest.dto;

import com.mysawit.harvest.model.HarvestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarvestResponse {
    private UUID id;
    private String plantationId;
    private UUID harvesterId;
    private UUID foremanId;
    private String harvesterName;
    private Double weight;
    private String news;
    private List<String> photos;
    private HarvestStatus status;
    private String rejectionReason;
    private LocalDateTime harvestDate;
    private LocalDateTime statusUpdatedDate;
}
