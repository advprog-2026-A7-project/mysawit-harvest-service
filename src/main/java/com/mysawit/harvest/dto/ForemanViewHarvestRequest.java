package com.mysawit.harvest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ForemanViewHarvestRequest {
    @Setter
    private String harvesterName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

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