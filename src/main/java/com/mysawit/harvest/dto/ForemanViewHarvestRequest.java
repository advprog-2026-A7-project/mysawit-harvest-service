package com.mysawit.harvest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
public class ForemanViewHarvestRequest {
    private String harvesterName;

    private LocalDate date;

}