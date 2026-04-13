package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HarvestRepository extends JpaRepository<Harvest, UUID> {
    // Harvester log
    boolean existsHarvestByHarvesterIdAndHarvestDateBetween(UUID harvesterId, LocalDateTime dayStart, LocalDateTime dayEnd);

    // Harvester view
    List<Harvest> findAllByHarvesterIdAndHarvestDateBetween(UUID harvesterId, LocalDateTime dayStart, LocalDateTime dayEnd);

    // Foreman view
    List<Harvest> findAllByForemanIdAndHarvesterNameContainingIgnoreCaseAndHarvestDateBetween(UUID foremanId, String harvesterName, LocalDateTime dayStart, LocalDateTime dayEnd);
    List<Harvest> findAllByForemanIdAndHarvesterNameContainingIgnoreCase(UUID foremanId, String name);
    List<Harvest> findAllByForemanIdAndHarvestDateBetween(UUID foremanId, LocalDateTime start, LocalDateTime end);
    List<Harvest> findAllByForemanId(UUID foremanId);

}