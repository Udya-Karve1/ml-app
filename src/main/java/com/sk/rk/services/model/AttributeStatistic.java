package com.sk.rk.services.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class AttributeStatistic implements Serializable {
    private String name;
    private Boolean date;
    private Boolean nominal;
    private Boolean numeric;
    private Boolean regular;
    private Boolean averagable;
    private String dateFormat;
    private Double count;
    private Double max;
    private Double min;
    private Double stdDev;
    private Double mean;
    private Double sum;
    private Integer distinct;
    private Integer unique;
    private Integer missing;
    private int[] nominalCount;
}
