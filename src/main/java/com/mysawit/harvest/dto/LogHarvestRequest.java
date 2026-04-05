package com.mysawit.harvest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LogHarvestRequest {
    @NotNull(message = "Plantation ID is required")
    private UUID plantationId;

    @NotBlank(message = "Harvester name is required")
    private String harvesterName;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    @NotNull(message = "News is required")
    private String news;

    private List<String> photos;
}
