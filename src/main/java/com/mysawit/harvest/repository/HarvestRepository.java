package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
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
    @Query("SELECT h FROM Harvest h WHERE h.harvesterId = :harvesterId " +
            "AND (:status IS NULL OR h.status = :status) " +
            "AND h.harvestDate BETWEEN :start AND :end")
    List<Harvest> findAllByHarvesterIdAndDateAndStatus(
            @Param("harvesterId") UUID harvesterId,
            @Param("status") HarvestStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Foreman view
    @Query("SELECT h FROM Harvest h WHERE h.foremanId = :foremanId " +
            "AND (:name IS NULL OR LOWER(h.harvesterName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:start IS NULL OR h.harvestDate >= :start) " +
            "AND (:end IS NULL OR h.harvestDate <= :end) " +
            "ORDER BY h.harvestDate DESC")
    List<Harvest> findAllByHarvesterNameAndDate(
            @Param("foremanId") UUID foremanId,
            @Param("name") String name,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}