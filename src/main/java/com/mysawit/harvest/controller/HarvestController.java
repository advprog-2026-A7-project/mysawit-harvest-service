package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.service.HarvestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/harvests")
public class HarvestController {
    
    private final HarvestService harvestService;
    
    public HarvestController(HarvestService harvestService) {
        this.harvestService = harvestService;
    }
    
    @GetMapping
    public ResponseEntity<List<Harvest>> getAllHarvests(
            @RequestParam(required = false) Long plantationId,
            @RequestParam(required = false) Long harvesterId) {
        List<Harvest> harvests;
        if (plantationId != null) {
            harvests = harvestService.getHarvestsByPlantationId(plantationId);
        } else if (harvesterId != null) {
            harvests = harvestService.getHarvestsByHarvesterId(harvesterId);
        } else {
            harvests = harvestService.getAllHarvests();
        }
        return ResponseEntity.ok(harvests);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getHarvestById(@PathVariable Long id) {
        try {
            Harvest harvest = harvestService.getHarvestById(id);
            return ResponseEntity.ok(harvest);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createHarvest(@Valid @RequestBody HarvestRequest request) {
        try {
            Harvest harvest = harvestService.createHarvest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(harvest);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHarvest(
            @PathVariable Long id,
            @Valid @RequestBody HarvestRequest request) {
        try {
            Harvest harvest = harvestService.updateHarvest(id, request);
            return ResponseEntity.ok(harvest);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHarvest(@PathVariable Long id) {
        try {
            harvestService.deleteHarvest(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Harvest deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "mysawit-harvest-service");
        return ResponseEntity.ok(health);
    }
}
