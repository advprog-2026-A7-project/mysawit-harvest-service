package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HarvestRepository extends JpaRepository<Harvest, UUID> {
    // Harvester log
    boolean existsHarvestByHarvesterIdAndHarvestDateBetween(UUID harvesterId, LocalDateTime dayStart, LocalDateTime dayEnd);

    // Harvester view
    @Query("SELECT h FROM Harvest h WHERE h.harvesterId = :id " +
            "AND (:start IS NULL OR h.harvestDate >= :start) " +
            "AND (:end IS NULL OR h.harvestDate <= :end) " +
            "ORDER BY h.harvestDate DESC")
    List<Harvest> findAllByHarvesterIdAndDate(
            @Param("id") UUID id,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Foreman view
    List<Harvest> findAllByForemanIdAndHarvesterNameContainingIgnoreCaseAndHarvestDateBetween(UUID foremanId, String harvesterName, LocalDateTime dayStart, LocalDateTime dayEnd);
    List<Harvest> findAllByForemanIdAndHarvesterNameContainingIgnoreCase(UUID foremanId, String name);
    List<Harvest> findAllByForemanIdAndHarvestDateBetween(UUID foremanId, LocalDateTime start, LocalDateTime end);
    List<Harvest> findAllByForemanId(UUID foremanId);

}