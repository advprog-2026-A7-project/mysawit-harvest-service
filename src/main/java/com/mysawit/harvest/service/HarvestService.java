package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.repository.HarvestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HarvestService {
    
    private final HarvestRepository harvestRepository;
    
    public HarvestService(HarvestRepository harvestRepository) {
        this.harvestRepository = harvestRepository;
    }
    
    public List<Harvest> getAllHarvests() {
        return harvestRepository.findAll();
    }
    
    public Harvest getHarvestById(Long id) {
        return harvestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Harvest not found with id: " + id));
    }
    
    public List<Harvest> getHarvestsByPlantationId(Long plantationId) {
        return harvestRepository.findByPlantationId(plantationId);
    }
    
    public List<Harvest> getHarvestsByHarvesterId(Long harvesterId) {
        return harvestRepository.findByHarvesterId(harvesterId);
    }
    
    public Harvest createHarvest(HarvestRequest request) {
        Harvest harvest = new Harvest();
        harvest.setPlantationId(request.getPlantationId());
        harvest.setHarvestDate(request.getHarvestDate());
        harvest.setWeight(request.getWeight());
        harvest.setQuality(request.getQuality() != null ? request.getQuality() : "STANDARD");
        harvest.setHarvesterId(request.getHarvesterId());
        harvest.setNotes(request.getNotes());
        
        return harvestRepository.save(harvest);
    }
    
    public Harvest updateHarvest(Long id, HarvestRequest request) {
        Harvest harvest = getHarvestById(id);
        
        harvest.setPlantationId(request.getPlantationId());
        harvest.setHarvestDate(request.getHarvestDate());
        harvest.setWeight(request.getWeight());
        harvest.setQuality(request.getQuality() != null ? request.getQuality() : "STANDARD");
        harvest.setHarvesterId(request.getHarvesterId());
        harvest.setNotes(request.getNotes());
        
        return harvestRepository.save(harvest);
    }
    
    public void deleteHarvest(Long id) {
        Harvest harvest = getHarvestById(id);
        harvestRepository.delete(harvest);
    }
}
