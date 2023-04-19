package com.sk.rk.services.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Correlation {
    private String attribute1;
    private String attribute2;
    private double value;
}
