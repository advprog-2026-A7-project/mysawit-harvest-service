package com.mysawit.harvest.mapper;

import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.model.Harvest;
import org.springframework.stereotype.Component;

@Component
public class HarvestMapper {
    public HarvestResponse mapToResponse(Harvest harvest) {
        return HarvestResponse.builder()
                .id(harvest.getId())
                .plantationId(harvest.getPlantationId())
                .harvesterId(harvest.getHarvesterId())
                .foremanId(harvest.getForemanId())
                .harvesterName(harvest.getHarvesterName())
                .weight(harvest.getWeight())
                .news(harvest.getNews())
                .photos(harvest.getPhotos())
                .status(harvest.getStatus())
                .rejectionReason(harvest.getRejectionReason())
                .harvestDate(harvest.getHarvestDate())
                .statusUpdatedDate(harvest.getStatusUpdatedDate())
                .build();
    }
}
