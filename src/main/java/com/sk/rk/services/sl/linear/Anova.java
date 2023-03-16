package com.sk.rk.services.sl.linear;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Anova {
	private int regression;
	private int residual;
	private double totalSumOfSquare;
	private int total;
	private double regressionSumOfSquare;
	private double regressionSumOfResidual;
	private double meanOfRegression;
	private double meanOfResidual;
	private double fValue;
	private double fSignificance;
}
