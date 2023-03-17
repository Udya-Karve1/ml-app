package com.sk.rk.services.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvException;
import com.sk.rk.services.exception.BaseException;
import com.sk.rk.services.exception.BaseRunTimeException;
import com.sk.rk.services.model.*;
import com.sk.rk.services.sl.linear.Regression;
import com.sk.rk.services.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.ClassifierAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.trees.J48;
import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MLService {

    @Autowired
    private StorageService storageService;

    @Autowired
    private CacheManagement cacheManagement;



    public Resource loadFileAsResource(String fileName, String fileCategory) throws MalformedURLException {
        Resource resource = storageService.downloadFile(fileCategory, fileName);
        if(resource.exists()) {
            return resource;
        } else {
            throw new BaseRunTimeException(400, "File not found " + fileName);
        }
    }



    public UserSession uploadCSV(MultipartFile file) throws Exception {

        String uploadedFileName = storageService.getFileNameToUpload(file);

        storageService.uploadFile(file, Constants.CSV);

        log.info("ActionPlan_File_url: {}", uploadedFileName);

        Resource resource = loadFileAsResource(uploadedFileName, Constants.CSV);

        return cacheManagement.createUserSession(resource.getFile().getAbsolutePath());
    }


    public Double toDoubleValue(String variableName, Map<String, String> typeMap, Object objValue) {
        if (typeMap.get(variableName).equals(Constants.BYTE) || typeMap.get(variableName).equals(Constants.SHORT)
                || typeMap.get(variableName).equals(Constants.INTEGER)
                || typeMap.get(variableName).equals(Constants.LONG)
                || typeMap.get(variableName).equals(Constants.BIG_INTEGER)) {

            return convertToDouble(objValue);
        } else {
            return (Double) objValue;
        }
    }

    private double convertToDouble(Object byteVal) {
        if (byteVal == null)
            return 0;

        return Double.valueOf(byteVal.toString());
    }


    public void prepareLinearRegressionModel() throws Exception {

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("E:\\file-upload\\csv\\insurance.arff");
        Instances dataset = source.getDataSet();

        dataset.setClassIndex(dataset.numAttributes()-1);

        LinearRegression model = new LinearRegression();
        model.buildClassifier(dataset);

        log.info("LR formula: {}", model);
        log.info("LR formula coefficients: {}", model.coefficients());
        Instance myhouse = dataset.lastInstance();
        double price = model.classifyInstance(myhouse);

    }


    public void loadCsv() throws IOException {

        /**
         * load csv file and save as arff file
         */
        CSVLoader csvLoader = new CSVLoader();
        csvLoader.setSource(new File("E:\\file-upload\\csv\\insurance_268_.csv"));

        Instances csvData = csvLoader.getDataSet();

        ArffSaver arffSaver = new ArffSaver();
        arffSaver.setInstances(csvData);
        arffSaver.setFile(new File("E:\\file-upload\\csv\\insurance_268_.arff"));
        arffSaver.writeBatch();
        //******************************************************
    }

    public void attributeFilter() throws Exception {

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("E:\\file-upload\\csv\\insurance - Copy.arff");
        Instances dataset = source.getDataSet();
        String[] remoteOpt = new String[]{"-R", "1"};
        Remove remove = new Remove();
        remove.setOptions(remoteOpt);
        remove.setInputFormat(dataset);

        Instances newData = Filter.useFilter(dataset, remove);
        ArffSaver saver = new ArffSaver();
        saver.setInstances(newData);
        saver.setFile(new File("E:\\file-upload\\csv\\insurance_new_remove.arff"));
        saver.writeBatch();


    }

    public void sparseDadta() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("");
        Instances dataset = source.getDataSet();
        NonSparseToSparse sp = new NonSparseToSparse();
        sp.setInputFormat(dataset);

        Instances newData = Filter.useFilter(dataset, sp);
        ArffSaver saver = new ArffSaver();
        saver.setInstances(newData);
        saver.setFile(new File("E:\\file-upload\\csv\\insurance_new_spares.arff"));
        saver.writeBatch();
    }

    public void attributeSelection() throws Exception {



        ConverterUtils.DataSource source = new ConverterUtils.DataSource("E:\\file-upload\\csv\\insurance - Copy.arff");
        Instances dataset = source.getDataSet();

        AttributeSelection filter = new AttributeSelection();
        CfsSubsetEval eval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);
        filter.setEvaluator(eval);
        filter.setSearch(search);
        filter.setInputFormat(dataset);
        Instances newData = Filter.useFilter(dataset, filter);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(newData);
        saver.setFile(new File("E:\\file-upload\\csv\\insurance_new_attribute_selection.arff"));
        saver.writeBatch();
    }


    public ConcurrentMap<?, Integer> getUniqueValuesWithCount(String sessionId, String fieldName) {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        Attribute attribute = dataset.attribute(fieldName);

        if(attribute==null) {
            throw new BaseRunTimeException(400, "Attribute " + fieldName + " not found.");
        }

        if(attribute.isNumeric()) {
            final ConcurrentMap<Double, Integer> distinctMap = new ConcurrentHashMap();
            for(int i = 0;i< dataset.size(); i++) {
                double doubleValue = dataset.get(i).value(attribute.index());
                Integer valueFromMap = distinctMap.get(doubleValue);
                if(valueFromMap==null) {
                    valueFromMap = 0;
                }
                ++valueFromMap;

                distinctMap.put(doubleValue, valueFromMap);
            }

            return distinctMap;
        } else {
            final ConcurrentMap<String, Integer> distinctMap = new ConcurrentHashMap();
            for(int i = 0;i< dataset.size(); i++) {
                String doubleValue = dataset.get(i).stringValue(attribute.index());
                Integer valueFromMap = distinctMap.get(doubleValue);
                if(valueFromMap==null) {
                    valueFromMap = 0;
                }
                ++valueFromMap;

                distinctMap.put(doubleValue, valueFromMap);
            }
            return distinctMap;
        }
    }

    public List<AttributeStatistic> getAttributeStat(String sessionId)  {
        Instances dataset = cacheManagement.getDatasource(sessionId);

        if(dataset.classIndex() == -1) {
            dataset.setClassIndex(dataset.numAttributes()-1);
        }

        int numAttr = dataset.numAttributes() - 1;
        List<AttributeStatistic> attributeList = new ArrayList<>(numAttr);

        for(int i=0; i<numAttr; i++) {
            Attribute attribute = dataset.attribute(i);
            AttributeStats as = dataset.attributeStats(i);

            AttributeStatistic atrSt = AttributeStatistic.builder()
                    .averagable(attribute.isAveragable())
                    .nominal(attribute.isNominal())
                    .name(attribute.name())
                    .regular(attribute.isRegular())
                    .date(attribute.isDate())
                    .numeric(attribute.isNumeric())
                    .count(as.numericStats!=null?as.numericStats.count:null)
                    .min(as.numericStats!=null?as.numericStats.min:null)
                    .mean(as.numericStats!=null?as.numericStats.mean:null)
                    .max(as.numericStats!=null?as.numericStats.max:null)
                    .stdDev(as.numericStats!=null?as.numericStats.stdDev:null)
                    .sum(as.numericStats!=null?as.numericStats.sum:null)
                    .distinct(as.distinctCount)
                    .unique(as.uniqueCount)
                    .nominalCount(as.nominalCounts)
                    .missing(as.missingCount)
                    .build();

            attributeList.add(atrSt);

        }

        return attributeList;
    }



    private void performLinearRegression(String sessionId) {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        dataset.setClassIndex(dataset.numAttributes() - 1);
    }

    private int getClassIndex(Instances dataset, String name) {
        return dataset.attribute(name).index();
    }

    private ClassificationResponse performLogistic(String sessionId, Request request) throws Exception {
        Instances dataset = cacheManagement.getTrainDataset(sessionId);
        dataset.setClassIndex(getClassIndex(dataset, request.getYColumn()));

        Ranker ranker = new Ranker();

        Logistic logistic = new Logistic();
        logistic.buildClassifier(dataset);



        Evaluation evaluation = new Evaluation(dataset);

        AttributeSelection attributeSelection = new AttributeSelection();



        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(logistic, testDataset);

        return buildClassificationResponse(evaluation);
    }

    private ClassificationResponse performDecisionTreeJ48(String sessionId, Request request) throws Exception {
        Instances dataset = cacheManagement.getTrainDataset(sessionId);
        dataset.setClassIndex(getClassIndex(dataset, request.getYColumn()));

        J48 tree = new J48();
        tree.buildClassifier(dataset);
        Evaluation evaluation = new Evaluation(dataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(tree, testDataset);

        return buildClassificationResponse(evaluation);

/*        log.info(evaluation.toSummaryString("Evaluation result:\n", false));
        log.info("PCF Correct: {}", evaluation.pctCorrect());
        log.info("PCT Incorrect: {}", evaluation.pctIncorrect());
        log.info("Area under ROC: {}", evaluation.areaUnderROC(1));
        log.info("KAPPA: {}", evaluation.kappa());
        log.info("Mean absolute error: {}", evaluation.meanAbsoluteError());
        log.info("Root mean prior squared error: {}", evaluation.rootMeanPriorSquaredError());
        log.info("Relative absolute error: {}", evaluation.relativeAbsoluteError());
        log.info("Root relative squared error: {}", evaluation.rootRelativeSquaredError());
        log.info("Precision: {}", evaluation.precision(1));
        log.info("Recall: {}", evaluation.recall(1));
        log.info("fMeasure: {}", evaluation.fMeasure(1));
        log.info("Error Rate: {}", evaluation.errorRate());
        log.info(evaluation.toMatrixString("==== Overall Confusion Matrix ====\n "));
        return evaluation.toSummaryString();*/
    }

    private ClassificationResponse performNaiveBayesClassification(String sessionId, Request request) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());

        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));
        NaiveBayes naiveBayes = new NaiveBayes();
        naiveBayes.buildClassifier(trainDataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        Evaluation evaluation = new Evaluation(trainDataset);
        evaluation.evaluateModel(naiveBayes, cacheManagement.getTestDataset(sessionId));

        return buildClassificationResponse(evaluation);
    }


    public ClassificationResponse doClassification(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        switch (request.getRegressionType()) {
            case "NaiveByes":
                return performNaiveBayesClassification(sessionId, request);
            case "Tree":
                return performDecisionTreeJ48(sessionId, request);
            case "Logistic":
                return performLogistic(sessionId, request);

            default:
                throw new BaseRunTimeException(400, "Specified algorithm " + request.getRegressionType() + " not found.");
        }
    }

    private void prepareTrainTestDataset(Request request, String sessionId) {
        UserSession userSession = cacheManagement.getUserSession(sessionId);
        Instances dataset = userSession.getDateset();

        int trainDateRecords = Math.round ((dataset.size() * request.getTrainDataSize())/100);

        userSession.setTrainDataset(new Instances(dataset, 0, trainDateRecords));
        userSession.setTestDataset(new Instances(dataset, trainDateRecords, (dataset.size()-trainDateRecords -1)));
    }

    private ClassificationResponse buildClassificationResponse(Evaluation evaluation) throws Exception {

        return ClassificationResponse.builder()
                .areaUnderRoc(evaluation.areaUnderROC(1))
                .PctCorrect(evaluation.pctCorrect())
                .PctIncorrect(evaluation.pctIncorrect())
                .kappa(evaluation.kappa())
                .meanAbsoluteError(evaluation.meanAbsoluteError())
                .rootMeanPeriodSquaredError(evaluation.rootMeanPriorSquaredError())
                .relativeAbsoluteError(evaluation.relativeAbsoluteError())
                .precision(evaluation.precision(1))
                .fmeasure(evaluation.fMeasure(1))
                .errorRate(evaluation.errorRate())
                .confusionMatrix(evaluation.confusionMatrix())
                .build();

    }

    public RegressionResponse doRegression(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());
        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));

        LinearRegression regression = new LinearRegression();
        regression.buildClassifier(trainDataset);


        return RegressionResponse.builder().message("testing message").build();

    }
}
