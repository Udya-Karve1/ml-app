package com.sk.rk.services.sl.linear;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Regression extends OLSMultipleLinearRegression {

	private RegressionCSTP intercepter;
	private RegressionCSTP[] regressionCSTPS;
	private Double rSquare;
	private Double estimateRegressionStandardError;
	private Double adjustedRSquared;
	private final Integer observations;
	private Double residualSumOfSquares;
	private Double totalSumOfSquares;
	private double[] estimateRegressionParameters;
	private double[] estimateRegressionParametersStandardErrors;
	private double[] y;
	private double[][] x;
	private double[] yTest;
	private double[][] xTest;

	public RegressionCSTP getIntercepter() {
		return intercepter;
	}

	public RegressionCSTP[] getRegressionCSTPS() {
		return regressionCSTPS;
	}

	public double getrSquare() {
		return rSquare;
	}

	public double getEstimateRegressionStandardError() {
		return estimateRegressionStandardError;
	}

	public double getAdjustedRSquared() {
		return adjustedRSquared;
	}

	public double getResidualSumOfSquares() {
		return residualSumOfSquares;
	}

	public double getTotalSumOfSquares() {
		return totalSumOfSquares;
	}

	private final String yTitle;
	private final String[] xTitles;
	private RegressionModel regressionModel;
	private Map<String, Double> coffMap = new HashMap<>();

	public Regression(String yTitle, String[] xTitles, double[] y, double[][] x, Integer trainRecords) {

		trainRecords = getTestRecords(y.length, trainRecords);
		int testRecord = y.length - (trainRecords + 1);
		this.y = new double[trainRecords];
		this.x = new double[trainRecords][xTitles.length];
		this.yTest = new double[testRecord];
		this.xTest = new double[testRecord][xTitles.length];
		System.arraycopy(y, 0, this.y, 0, trainRecords);
		System.arraycopy(y, trainRecords, this.yTest, 0, testRecord);

		System.arraycopy(x, 0, this.x, 0, trainRecords);
		System.arraycopy(x, trainRecords, this.xTest, 0, testRecord);

		this.yTitle = yTitle;
		this.xTitles = xTitles;

		super.newSampleData(y, x);
		this.observations = y.length;
		this.performStatistics();
		this.calculateCoefficient();
		this.regressionModel = regressionModel();
		this.getCoefficient();
	}

	private void performStatistics() {
		adjustedRSquared = super.calculateAdjustedRSquared();
		rSquare = super.calculateRSquared();
		residualSumOfSquares = super.calculateResidualSumOfSquares();
		totalSumOfSquares = super.calculateTotalSumOfSquares();
		estimateRegressionStandardError = super.estimateRegressionStandardError();
		estimateRegressionParameters = super.estimateRegressionParameters();
		estimateRegressionParametersStandardErrors = super.estimateRegressionParametersStandardErrors();
	}

	private void calculateCoefficient() {
		int residualdf = super.estimateResiduals().length - estimateRegressionParameters.length;
		final TDistribution tdistribution = new TDistribution(residualdf);

		regressionCSTPS = new RegressionCSTP[estimateRegressionParameters.length - 1];

		for (int i = 0; i < estimateRegressionParameters.length; i++) {
			double tstat = estimateRegressionParameters[i] / estimateRegressionParametersStandardErrors[i];
			double pvalue = tdistribution.cumulativeProbability(-FastMath.abs(tstat)) * 2;

			if (i == 0) {
				intercepter = new RegressionCSTP();
				intercepter.setTStat(tstat);
				intercepter.setPValue(pvalue);
				intercepter.setCoefficient(estimateRegressionParameters[0]);
				intercepter.setStandardError(estimateRegressionParametersStandardErrors[0]);
				intercepter.setFieldName(this.yTitle);
			} else {
				RegressionCSTP regressionCSTP = new RegressionCSTP();
				regressionCSTP.setTStat(tstat);
				regressionCSTP.setPValue(pvalue);
				regressionCSTP.setCoefficient(estimateRegressionParameters[i]);
				regressionCSTP.setStandardError(estimateRegressionParametersStandardErrors[i]);
				regressionCSTP.setFieldName(this.xTitles[i - 1]);
				regressionCSTPS[i - 1] = regressionCSTP;
			}
		}
	}

	public RegressionModel regressionModel() {
		regressionModel = new RegressionModel();
		regressionModel.setInterceptor(this.getIntercepter());
		regressionModel.setRegressionCSTPS(this.getRegressionCSTPS());
		regressionModel.setRSquare(this.getrSquare());
		regressionModel.setStandardError(this.getEstimateRegressionStandardError());
		regressionModel.setAdjustedRSquared(this.getAdjustedRSquared());
		regressionModel.setObservations(this.observations);

		Anova anova = new Anova();

		int regression = this.x[0].length;
		int residual = this.y.length - (regression + 1);
		anova.setRegression(regression);
		anova.setResidual(residual);
		anova.setTotal((residual + regression));
		anova.setRegressionSumOfResidual(this.getResidualSumOfSquares());
		anova.setTotalSumOfSquare(this.getTotalSumOfSquares());
		double regressionSumOfSquare = this.getTotalSumOfSquares() - this.getResidualSumOfSquares();
		anova.setRegressionSumOfSquare(regressionSumOfSquare);

		double msr = regressionSumOfSquare / regression;
		double mse = this.getResidualSumOfSquares() / residual;

		anova.setMeanOfResidual(mse);
		anova.setMeanOfRegression(msr);
		double fvalue = msr / mse;
		anova.setFValue(fvalue);
		regressionModel.setAnova(anova);

		anova.setFSignificance(pf(fvalue, regression, residual));

		return regressionModel;
	}

	private Double betacf(double a, double b, double x) {
		Double qab = a + b;
		Double qap = a + 1.0;
		Double qam = a - 1.0;
		Double c = 1.0;
		Double d = 1 - qab * x / qap;
		Double fpmin = 1.e-300;
		Double eps = 1.e-6;
		if (Math.abs(d) < fpmin) {
			d = fpmin;
		}
		d = 1.0 / d;

		Double h = d;
		for (int m = 1; m < 1001; m++) {
			int m2 = 2 * m;
			Double aa = m * (b - m) * x / ((qam + m2) * (a + m2));

			d = 1 + aa * d;
			if (Math.abs(d) < fpmin) {
				d = fpmin;
			}
			c = 1 + aa / c;
			if (Math.abs(c) < fpmin) {
				c = fpmin;
			}
			d = 1.0 / d;
			h = h * d * c;
			aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
			d = 1 + aa * d;
			if (Math.abs(d) < fpmin) {
				d = fpmin;
			}
			c = 1 + aa / c;
			if (Math.abs(c) < fpmin) {
				c = fpmin;
			}
			d = 1.0 / d;
			double del = d * c;
			h = h * del;
			if (Math.abs(del - 1.0) < eps) {
				break;
			}
		}
		return h;
	}

	private Double betai(int n, int m, double x) {
		Double bt;
		Double a = 0.5 * n;
		Double b = 0.5 * m;
		if (x == 0 || x == 1) {
			bt = 0.0;
		} else {
			bt = Math.exp(gamnln(m + n) - gamnln(n) - gamnln(m) + a * Math.log(x) + b * Math.log(1 - x));
		}
		Double beti;
		if (x < (a + 1.0) / (a + b + 2)) {
			// use continued fraction directly
			beti = bt * betacf(a, b, x) / a;
		} else {
			// use continued fraction after making the symmetry transformation
			beti = 1.0 - bt * betacf(b, a, 1 - x) / b;
		}
		return beti;
	}

	private Double gamnln(int n) {

		if (n < 201) {
			return RegressionConstants.LG[n - 1];
		}

		Double xVal = 0.5 * n;
		Double yVal = xVal;
		Double tmp = xVal + 5.5;
		tmp = (xVal + 0.5) * Math.log(tmp) - tmp;
		Double ser = 1.000000000190015;
		for (int i = 0; i < 6; i++) {
			yVal = yVal + 1;
			ser = ser + RegressionConstants.COEF[i] / yVal;
		}

		return tmp + Math.log(RegressionConstants.STP * ser / xVal);
	}

	private Double pf(Double fVal, int df1, int df2) {
		if (fVal == 0) {
			return 1.0;
		}

		double xVal = df2 / (df1 * fVal + df2);
		return betai(df2, df1, xVal);
	}

	private Integer getTestRecords(Integer totalRecords, Integer trainRecords) {
		return Math.abs((trainRecords * totalRecords) / 100);
	}

	public Map<String, Object> estimateTestData() {
		List<Map<String, Object>> estimateRecords = new ArrayList<>();
		double[] estimateArray = new double[this.xTest.length];

		for (int i = 0; i < this.xTest.length; i++) {
			Map<String, Object> map = new HashMap<>();
			Double estimateVal = 0.0;
			for (int j = 0; j < this.xTitles.length; j++) {
				map.put(this.xTitles[j], this.xTest[i][j]);
				estimateVal = estimateVal + (this.xTest[i][j] * this.coffMap.get(this.xTitles[j]));

			}

			estimateVal = estimateVal + intercepter.getCoefficient();
			estimateArray[i] = estimateVal;
			map.put(this.yTitle + "_Actual", this.yTest[i]);
			map.put(this.yTitle + "_Pred", estimateVal);

			estimateRecords.add(map);
		}

		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("estimateRecords", estimateRecords);

		return returnMap;
	}

	public RegressionModel getRegressionModel() {
		return this.regressionModel;
	}

	private void getCoefficient() {

		for (int i = 0; i < this.regressionModel.getRegressionCSTPS().length; i++) {
			coffMap.put(this.regressionModel.getRegressionCSTPS()[i].getFieldName(),
					this.regressionModel.getRegressionCSTPS()[i].getCoefficient());
		}
	}
}
