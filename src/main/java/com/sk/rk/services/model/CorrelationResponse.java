package com.sk.rk.services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CorrelationResponse {
    private List<Correlation> correlationList = new ArrayList<>();
    private double[][] correlationMatrix;
    private List<String> attributes = new ArrayList<>();
}
