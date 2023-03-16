package com.sk.rk.services.sl.linear;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Model {

	private RegressionCSTP interceptor;
	private RegressionCSTP[] regressionCSTPS;

	private Double adjustedRSquared;
	private Double rSquare;
	private Integer observations;
	private Double standardError;
	private Map<String, Object> confusionMatrix;

	private Anova anova;
}
