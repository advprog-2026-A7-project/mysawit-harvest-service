package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HarvestRepository extends JpaRepository<Harvest, Long> {
    List<Harvest> findByPlantationId(Long plantationId);
    List<Harvest> findByHarvesterId(Long harvesterId);
    List<Harvest> findByQuality(String quality);
}
