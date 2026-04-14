package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.service.HarvestService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/harvests")
@RequiredArgsConstructor
public class HarvestController {
    private final HarvestService harvestService;

    @PostMapping
    public ResponseEntity<HarvestResponse> logHarvest(
            @Valid @RequestBody LogHarvestRequest request,
            @RequestHeader("X-Harvester-Id") UUID harvesterId,
            @RequestHeader("X-Harvester-Name") String harvesterName,
            @RequestHeader("X-Foreman-Id") UUID foremanId
            ) {
        HarvestResponse response = harvestService.logHarvest(request, harvesterId, foremanId, harvesterName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @PatchMapping("/update")
    public ResponseEntity<HarvestResponse> updateStatus(
            @Valid @RequestBody UpdateHarvestStatusRequest request,
            @RequestHeader(value = "X-Foreman-Id", required = false) UUID foremanId
    ) {
        HarvestResponse response = harvestService.updateHarvestStatus(request, foremanId);
        return ResponseEntity.ok(response);
    }


    // TODO: nama endpoint plan
    // /harvests = buat log harvestny <done>
    // /harvests/my = harvester liat log dia ndiri <done>
    // /harvests = mandor liat semua history log harvesterny dan bs filtering <done>
    // /harvests/harvester/{harvesterId} = mandor liat one specific harvester beserta harvestny
    // /harvests/update/ = mandor change status
}