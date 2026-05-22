package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HarvestRepository extends JpaRepository<Harvest, UUID> {
    boolean existsHarvestByHarvesterIdAndHarvestDateBetween(UUID harvesterId, LocalDateTime dayStart, LocalDateTime dayEnd);

    @Query("SELECT h FROM Harvest h WHERE h.harvesterId = :harvesterId " +
            "AND (:status IS NULL OR h.status = :status) " +
            "AND (cast(:start as timestamp) IS NULL OR h.harvestDate >= :start) " +
            "AND (cast(:end as timestamp) IS NULL OR h.harvestDate <= :end) " +
            "ORDER BY h.harvestDate DESC")
    List<Harvest> findAllByHarvesterIdAndDateAndStatus(
            @Param("harvesterId") UUID harvesterId,
            @Param("status") HarvestStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT h FROM Harvest h WHERE h.foremanId = :foremanId " +
            "AND (cast(:name as text) IS NULL OR LOWER(h.harvesterName) LIKE LOWER(CONCAT('%', cast(:name as text), '%'))) " +
            "AND (cast(:date as text) IS NULL OR cast(h.harvestDate as date) = :date) " +
            "ORDER BY h.harvestDate DESC")
    List<Harvest> findAllByHarvesterNameAndDate(
            @Param("foremanId") UUID foremanId,
            @Param("name") String name,
            @Param("date") LocalDate date
    );
}