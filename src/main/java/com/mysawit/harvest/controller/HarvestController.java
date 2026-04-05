package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.dto.HarvesterViewHarvestRequest;
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
            @RequestHeader("X-Foreman-Id") UUID foremanId
            ) {
        HarvestResponse response = harvestService.logHarvest(request, harvesterId, foremanId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<HarvestResponse>> viewMyHistory(
            @ModelAttribute HarvesterViewHarvestRequest request,
            @RequestHeader(value = "X-Harvester-Id", required = false) UUID harvesterId
            ) {
        return ResponseEntity.ok(harvestService.harvesterViewHarvest(request, harvesterId, null));
    }


    // TODO: nama endpoint plan
    // /harvests = buat log harvestny <don>
    // /harvests/my = harvester liat log dia ndiri <don>
    // /harvests = mandor liat semua history log harvesterny dan bs filtering
    // /harvests/harvester/{harvesterId} = mandor liat one specific harvester beserta harvestny
}