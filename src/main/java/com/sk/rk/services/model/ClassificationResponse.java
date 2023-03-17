package com.sk.rk.services.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassificationResponse {
    @JsonProperty("PCFCorrect")
    private double PctCorrect;
    @JsonProperty("PCFCorrect-percent")
    private double PcfCorrectPercent;
    @JsonProperty("PCTIncorrect")
    private double PctIncorrect;
    @JsonProperty("PCTIncorrect-percent")
    private double PctIncorrectPercent;
    @JsonProperty("AreaUnderROC")
    private double areaUnderRoc;
    @JsonProperty("KAPPA")
    private double kappa;
    @JsonProperty("MeanAbsoluteError")
    private double meanAbsoluteError;
    @JsonProperty("RootMeanPeriodSquaredError")
    private double rootMeanPeriodSquaredError;
    @JsonProperty("RelativeAbsoluteError")
    private double relativeAbsoluteError;
    @JsonProperty("Precision")
    private double precision;
    @JsonProperty("FMeasure")
    private double fmeasure;
    @JsonProperty("ErrorRate")
    private double errorRate;

    private double[][] confusionMatrix;
}
