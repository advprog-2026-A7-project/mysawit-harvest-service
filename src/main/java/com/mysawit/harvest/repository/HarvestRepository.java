package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface HarvestRepository extends JpaRepository<Harvest, UUID> {
    boolean existsHarvestByHarvesterIdAndHarvestDateBetween(UUID harvesterId, LocalDateTime dayStart, LocalDateTime dayEnd);
}