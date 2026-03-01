package com.mysawit.harvest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Entity
@Table(name = "harvests")
public class Harvest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Plantation ID is required")
    @Column(name = "plantation_id", nullable = false)
    private Long plantationId;
    
    @NotNull(message = "Harvest date is required")
    @Column(name = "harvest_date", nullable = false)
    private LocalDateTime harvestDate;
    
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    @Column(nullable = false)
    private Double weight; // in kg
    
    @Column(nullable = false)
    private String quality = "STANDARD"; // PREMIUM, STANDARD, LOW
    
    @Column(name = "harvester_id")
    private Long harvesterId;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
