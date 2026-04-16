package com.mysawit.harvest.dto;

import com.mysawit.harvest.model.HarvestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHarvestStatusRequest {
    @NotNull(message = "Harvest log ID is required")
    private UUID id;

    @NotNull(message = "Status is required")
    private HarvestStatus status;

    private String rejectionReason;
}