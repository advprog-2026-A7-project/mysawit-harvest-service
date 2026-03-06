package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.service.HarvestService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/harvests")
@RequiredArgsConstructor
public class HarvestController {

    private final HarvestService harvestService;

    @PostMapping
    public ResponseEntity<?> logHarvest(
            @Valid @RequestBody HarvestRequest request,
            @RequestHeader("X-Harvester-Id") UUID harvesterId,
            @RequestHeader("X-Foreman-Id") UUID foremanId
    ) {
        HarvestResponse response = harvestService.logHarvest(request, harvesterId, foremanId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}