package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.service.HarvestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.ok(harvestService.getHarvestById(id));
        } catch (RuntimeException e) {
            return notFoundError(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createHarvest(@Valid @RequestBody HarvestRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(harvestService.createHarvest(request));
        } catch (RuntimeException e) {
            return badRequestError(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHarvest(@PathVariable Long id, @Valid @RequestBody HarvestRequest request) {
        try {
            return ResponseEntity.ok(harvestService.updateHarvest(id, request));
        } catch (RuntimeException e) {
            return notFoundError(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHarvest(@PathVariable Long id) {
        try {
            harvestService.deleteHarvest(id);
            return ResponseEntity.ok(Map.of("message", "Harvest deleted successfully"));
        } catch (RuntimeException e) {
            return notFoundError(e);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "mysawit-harvest-service"));
    }

    private ResponseEntity<Map<String, String>> notFoundError(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    private ResponseEntity<Map<String, String>> badRequestError(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
