package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.service.HarvestService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/harvests")
@RequiredArgsConstructor
public class HarvestController {
    private final HarvestService harvestService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> logHarvest(
            @Valid @RequestBody LogHarvestRequest request,
            @RequestHeader("X-Harvester-Id") UUID harvesterId,
            @RequestHeader("X-Harvester-Name") String harvesterName,
            @RequestHeader("X-Foreman-Id") UUID foremanId
    ) {
        HarvestResponse response = harvestService.logHarvest(request, harvesterId, foremanId, harvesterName);

        Map<String, Object> responseBody = Map.of(
                "message", "Harvest successfully logged",
                "id", response.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping("/my")
    public ResponseEntity<List<HarvestResponse>> viewMyHistory(
            @ModelAttribute HarvesterViewHarvestRequest request,
            @RequestHeader(value = "X-Harvester-Id", required = false) UUID harvesterId
            ) {
        return ResponseEntity.ok(harvestService.harvesterViewHarvest(request, harvesterId, null));
    }

    @GetMapping
    public ResponseEntity<List<HarvestResponse>> viewAllHistory(
            @ModelAttribute ForemanViewHarvestRequest request,
            @RequestHeader(value = "X-Foreman-Id", required = false) UUID foremanId,
            @RequestHeader(value = "X-Harvester-Id", required = false) UUID harvesterId
    ) {
        List<HarvestResponse> responses = harvestService.foremanViewHarvest(request, harvesterId, foremanId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HarvestResponse> getHarvestDetail(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Harvester-Id", required = false) UUID harvesterId,
            @RequestHeader(value = "X-Foreman-Id", required = false) UUID foremanId
    ) {

        HarvestResponse response = harvestService.getHarvestDetail(id, harvesterId, foremanId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update")
    public ResponseEntity<HarvestResponse> updateStatus(
            @Valid @RequestBody UpdateHarvestStatusRequest request,
            @RequestHeader(value = "X-Foreman-Id", required = false) UUID foremanId
    ) {
        HarvestResponse response = harvestService.updateHarvestStatus(request, foremanId);
        return ResponseEntity.ok(response);
    }
}