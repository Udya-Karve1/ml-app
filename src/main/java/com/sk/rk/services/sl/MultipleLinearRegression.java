package com.sk.rk.services.sl;

import com.sk.rk.services.sl.linear.Anova;
import com.sk.rk.services.sl.linear.RegressionCSTP;
import com.sk.rk.services.sl.linear.RegressionConstants;
import com.sk.rk.services.sl.linear.RegressionModel;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.util.FastMath;
import weka.classifiers.evaluation.RegressionAnalysis;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.Tag;
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipleLinearRegression extends LinearRegression {

    private int df;
    private double rSquared;
    private double rSquaredAdj;
    private double fStat;
    private double[] stdErrorOfCoefficient;
    private double[] tStats;
    protected double[] coefficients;


    private Instances dataset;
    private boolean[] selectedAttribute;



    public void prepareModel(Instances dataset) throws Exception {
        this.dataset = dataset;
        super.setOutputAdditionalStats(true);
        super.m_TransformedData = dataset;
        super.buildClassifier(dataset);
        prepareEvaluations();
    }


    private void prepareEvaluations() throws Exception {
        double se = calculateSECustom(super.m_SelectedAttributes, super.m_Coefficients);

        rSquared = RegressionAnalysis.calculateRSquared(super.m_TransformedData, se);
        rSquaredAdj = RegressionAnalysis.calculateAdjRSquared(rSquared, m_TransformedData.numInstances(), this.dataset.size());
        fStat = RegressionAnalysis.calculateFStat(rSquared, m_TransformedData.numInstances(), this.dataset.size());
        stdErrorOfCoefficient = RegressionAnalysis.calculateStdErrorOfCoef(m_TransformedData , m_SelectedAttributes, se, m_TransformedData.numInstances(), this.dataset.size());
        tStats = RegressionAnalysis.calculateTStats(m_Coefficients, stdErrorOfCoefficient, this.dataset.size());
        this.coefficients = super.m_Coefficients;
        this.selectedAttribute = super.m_SelectedAttributes;
    }

    protected double calculateSECustom(boolean[] selectedAttributes,
                                 double[] coefficients) throws Exception {

        double mse = 0;
        for (int i = 0; i < super.m_TransformedData.numInstances(); i++) {
            double prediction =
                    regressionPrediction(super.m_TransformedData.instance(i), selectedAttributes,
                            coefficients);
            double error = prediction - super.m_TransformedData.instance(i).classValue();
            mse += error * error;
        }
        return mse;
    }
    public double getRSquared() {
        return rSquared;
    }

    public double getRSquaredAdj() {
        return rSquaredAdj;
    }

    public double getFStat() {
        return fStat;
    }

    public double[] getStdErrorOfCoefficient() {
        return stdErrorOfCoefficient;
    }

    public double[] getTStats() {
        return tStats;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public boolean[] getSelectedAttribute() {
        return selectedAttribute;
    }
}
