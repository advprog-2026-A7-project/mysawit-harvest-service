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

    // Validasi satu hari sekali per buruh
    @Query("""
        SELECT COUNT(h) > 0 FROM Harvest h
        WHERE h.harvesterId = :harvesterId
        AND h.harvestDate >= :startOfDay
        AND h.harvestDate <= :endOfDay
    """)
    boolean existsTodaysHarvestByHarvesterId(
            @Param("harvesterId") UUID harvesterId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // Buruh lihat hasil panen sendiri dan filter
    @Query("""
        SELECT h FROM Harvest h
        WHERE h.harvesterId = :harvesterId
        AND (:startDate IS NULL OR h.harvestDate >= :startDate)
        AND (:endDate IS NULL OR h.harvestDate <= :endDate)
        AND (:status IS NULL OR h.status = :status)
        ORDER BY h.harvestDate DESC
    """)
    List<Harvest> findAllHarvestsByHarvesterId(
            @Param("harvesterId") UUID harvesterId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") HarvestStatus status
    );

    // Mandor lihat hasil panen buruh dan filter
    // TODO: tambahin filter harvesterName nanti
    @Query("""
        SELECT h FROM Harvest h
        WHERE h.foremanId = :foremanId
        AND (:harvestDate IS NULL OR CAST(h.harvestDate AS date) = CAST(:harvestDate AS date))
        ORDER BY h.harvestDate DESC
    """)
    List<Harvest> findAllHarvestsByForemanId(
            @Param("foremanId") UUID foremanId,
            @Param("harvestDate") LocalDateTime harvestDate
    );
}