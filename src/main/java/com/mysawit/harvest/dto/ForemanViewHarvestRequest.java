package com.mysawit.harvest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ForemanViewHarvestRequest {
    @Setter
    private String harvesterName;

    private LocalDate date;

    public void setDate(LocalDate date) {
        this.date = date;
    }
}