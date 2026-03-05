package com.mysawit.harvest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "harvests")
public class Harvest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Plantation ID is required")
    @Column(name = "plantation_id", nullable = false)
    private UUID plantationId;

    @NotNull(message = "Harvester ID is required")
    @Column(name = "harvester_id", nullable = false)
    private UUID harvesterId;

    @Column(name = "foreman_id")
    private UUID foremanId;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    @Column(nullable = false)
    private Double weight; // in KG

    @NotNull(message = "News is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String news;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "photos", columnDefinition = "text[]")
    private List<String> photos;

    @Column(nullable = false)
    private String status = "Pending";

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "harvest_date", updatable = false)
    private LocalDateTime harvestDate;

    @UpdateTimestamp
    @Column(name = "status_updated_date")
    private LocalDateTime statusUpdatedDate;

}
