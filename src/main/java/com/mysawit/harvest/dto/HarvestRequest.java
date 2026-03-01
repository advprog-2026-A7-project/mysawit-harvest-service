package com.mysawit.harvest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class HarvestRequest {
    
    @NotNull(message = "Plantation ID is required")
    private Long plantationId;
    
    @NotNull(message = "Harvest date is required")
    private LocalDateTime harvestDate;
    
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    private String quality;
    private Long harvesterId;
    private String notes;
    
    // Getters and Setters
    public Long getPlantationId() {
        return plantationId;
    }
    
    public void setPlantationId(Long plantationId) {
        this.plantationId = plantationId;
    }
    
    public LocalDateTime getHarvestDate() {
        return harvestDate;
    }
    
    public void setHarvestDate(LocalDateTime harvestDate) {
        this.harvestDate = harvestDate;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public String getQuality() {
        return quality;
    }
    
    public void setQuality(String quality) {
        this.quality = quality;
    }
    
    public Long getHarvesterId() {
        return harvesterId;
    }
    
    public void setHarvesterId(Long harvesterId) {
        this.harvesterId = harvesterId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
