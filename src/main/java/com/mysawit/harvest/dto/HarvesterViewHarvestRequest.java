package com.mysawit.harvest.dto;

import com.mysawit.harvest.model.HarvestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
public class HarvesterViewHarvestRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Getter @Setter
    private HarvestStatus status;

    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    public void setStartDate(LocalDateTime start) {
        if (start != null) {
            this.startDate = start.with(LocalTime.MIN);
        }
    }

    public void setEndDate(LocalDateTime end) {
        if (end != null) {
            this.endDate = end.with(LocalTime.MAX);
        }
    }
}
