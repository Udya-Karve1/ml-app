package com.sk.rk.services.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class RegressionResponse {

    private double rSquared;
    private double rSquaredAdj;
    private double fStat;
    private double[] stdErrorOfCoefficient;
    private double[] tStats;
    protected double[] coefficients;
    private double correlationCoefficient;
    private double meanAbsoluteError;
    private double rootMeanSquaredError;
    private double relativeAbsoluteError;
    private double rootRelativeSquaredError;
    private int  totalNumberOfInstance;
    private boolean[] selectedAttributes;
    private String summary;
    private List<String> selectedAttributeNames = new ArrayList<>();
}
