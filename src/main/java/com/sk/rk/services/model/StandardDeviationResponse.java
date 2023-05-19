package com.sk.rk.services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StandardDeviationResponse {
    private String attributeName;
    private StandardDeviation first;
    private StandardDeviation second;
    private StandardDeviation third;


    @Data
    @AllArgsConstructor
    public static class StandardDeviation {
        double upperLimit;
        double lowerLimit;
    }
}


