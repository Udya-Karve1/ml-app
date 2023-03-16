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
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
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

@Service
@Slf4j
public class MLService {

    @Autowired
    private StorageService storageService;

    @Autowired
    private DatabaseService databaseService;

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



    public String uploadCSV(MultipartFile file, String tableName) throws Exception {

        String uploadedFileName = storageService.getFileNameToUpload(file);

        storageService.uploadFile(file, Constants.CSV);

        log.info("ActionPlan_File_url: {}", uploadedFileName);

        Resource resource = loadFileAsResource(uploadedFileName, Constants.CSV);
        //processCsvFile(resource.getInputStream() , tableName);

        return cacheManagement.createUserSession(resource.getFile().getAbsolutePath()).getSessionId();
    }


/*
    private void processCsvFile(InputStream in, String tableName) throws BaseException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CsvService csvService = new CsvService(null, in);
            String jsonObject = csvService.getStringFormatValues();
            String dataTypes = csvService.geDataTypes();
            List<Map<String, Object>> values = csvService.getCSVValues();

            databaseService.createTable(tableName, mapper.readValue(dataTypes, new TypeReference<List<Map<String, String>>>() {}));
            databaseService.insertData(tableName, values);
            log.debug("DataTypes: {}", dataTypes);


        } catch (IOException | CsvException e) {
            throw new BaseException(400, e.getMessage(), e);
        }
    }

*/



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

    private Map<String, String> prepareTypeMap(Map<String, Object> mapVal) {
        Map<String, String> mapType = new HashMap<>();
        Iterator<String> it = mapVal.keySet().iterator();

        while (it.hasNext()) {
            String val = it.next();
            if (mapVal.get(val) instanceof Byte) {
                mapType.put(val, Constants.BYTE);
            } else if (mapVal.get(val) instanceof Short) {
                mapType.put(val, Constants.SHORT);
            } else if (mapVal.get(val) instanceof Integer) {
                mapType.put(val, Constants.INTEGER);
            } else if (mapVal.get(val) instanceof Long) {
                mapType.put(val, Constants.LONG);
            } else if (mapVal.get(val) instanceof BigInteger) {
                mapType.put(val, Constants.BIG_INTEGER);
            } else {
                mapType.put(val, "Double");
            }
        }
        return mapType;
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
        System.out.println("-------------------------");
        System.out.println("PRECTING THE PRICE : "+price);

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



    public List<AttributeStatistic> getAttributeStat(String sessionId) throws Exception {
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

    private void performDecisionTreeJ48(String sessionId) throws Exception {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        J48 tree = new J48();
        Evaluation evaluation = new Evaluation(dataset);

        ConverterUtils.DataSource dataSourceTest = new ConverterUtils.DataSource("");
        Instances testDataset = dataSourceTest.getDataSet();
        testDataset.setClassIndex(testDataset.numAttributes() - 1);
        evaluation.evaluateModel(tree, testDataset);

        log.info(evaluation.toSummaryString("Evaluation result:\n", false));
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
    }

    private void performNaiveBayesClassification(String sessionId) throws Exception {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        dataset.setClassIndex(dataset.numAttributes() - 1);
        NaiveBayes naiveBayes = new NaiveBayes();
        naiveBayes.buildClassifier(dataset);

        log.info("naiveBayes.getCapabilities(): {}", naiveBayes.getCapabilities());
    }


    public void doClassification(String sessionId, Request request) throws Exception {
        performNaiveBayesClassification(sessionId);
    }

    private Instances preProcessData(Request request, Instances dataset) {
        PreProcessData data = new PreProcessData();
        int totalSize = dataset.size();
        int totalTrainData = Math.round((request.getTrainDataSize()*100)/totalSize);

        Instances instances = new Instances(dataset);

        for(int i=totalSize; i>totalTrainData; i--) {
            instances.delete(i);
        }

        return instances;
    }
}
