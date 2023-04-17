package com.sk.rk.services.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.rk.services.exception.BaseRunTimeException;
import com.sk.rk.services.model.*;
import com.sk.rk.services.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.associations.Apriori;
import weka.associations.AssociatorEvaluation;
import weka.associations.FPGrowth;
import weka.attributeSelection.*;
import weka.attributeSelection.PrincipalComponents;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.RandomForest;
import weka.clusterers.*;
import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;
import weka.filters.supervised.attribute.AttributeSelection;

import weka.filters.unsupervised.attribute.*;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

        storageService.getFileNameToUpload(file);
        String fileName = file.getOriginalFilename();


        String uploadedFileName = storageService.uploadFile(file, Constants.CSV);

        log.info("ActionPlan_File_url: {}", uploadedFileName);

        Resource resource = loadFileAsResource(uploadedFileName, Constants.CSV);

        return cacheManagement.createUserSession(resource.getFile().getAbsolutePath(), fileName, uploadedFileName);
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


    public ConcurrentMap<Object, Integer> getUniqueValuesWithCount(String sessionId, String fieldName) {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        Attribute attribute = dataset.attribute(fieldName);

        if(attribute==null) {
            throw new BaseRunTimeException(400, "Attribute " + fieldName + " not found.");
        }

        if(attribute.isNumeric()) {
            final ConcurrentMap<Object, Integer> distinctMap = new ConcurrentHashMap();
            for(int i = 0;i< dataset.size(); i++) {
                Double doubleValue = dataset.get(i).value(attribute.index());
                Integer valueFromMap = distinctMap.get(doubleValue);
                if(valueFromMap==null) {
                    valueFromMap = 0;
                }
                ++valueFromMap;

                distinctMap.put(doubleValue, valueFromMap);
            }

            return distinctMap;
        } else {
            final ConcurrentMap<Object, Integer> distinctMap = new ConcurrentHashMap();
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
        if(null!=name && Strings.isNotBlank(name)) {
            for(int i=0; i<dataset.numAttributes(); i++) {
                if(dataset.attribute(i).name().equalsIgnoreCase(name)) {
                    return i;
                }
            }
        }
        throw new BaseRunTimeException(400 , "Class attribute not found: " + name);
    }

    private ClassificationResponse performLogistic(String sessionId, Request request) throws Exception {
        Instances dataset = cacheManagement.getTrainDataset(sessionId);
        dataset.setClassIndex(getClassIndex(dataset, request.getYColumn()));

        Logistic logistic = new Logistic();
        logistic.buildClassifier(dataset);

        Evaluation evaluation = new Evaluation(dataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(logistic, testDataset);

        return buildClassificationResponse(evaluation);
    }


    private ClassificationResponse randomForest(String sessionId, Request request) throws Exception {
        Instances dataset = cacheManagement.getTrainDataset(sessionId);
        dataset.setClassIndex(getClassIndex(dataset, request.getYColumn()));

        RandomForest randomForest = new RandomForest();
        randomForest.buildClassifier(dataset);

        Evaluation evaluation = new Evaluation(dataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(randomForest, testDataset);

        log.info("Random Forest {}", randomForest.getCapabilities());

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

    public ClassificationResponse performKNearestNeighbors(String sessionId, Request request) {
/*        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());

        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));
        KNN
        .buildClassifier(trainDataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        Evaluation evaluation = new Evaluation(trainDataset);
        evaluation.evaluateModel(naiveBayes, cacheManagement.getTestDataset(sessionId));

        return buildClassificationResponse(evaluation);*/

        return null;
    }
    public ClassificationResponse performRandomForest(String sessionId, Request request) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());

        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));
        RandomForest randomForest = new RandomForest();
        randomForest.buildClassifier(trainDataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        Evaluation evaluation = new Evaluation(trainDataset);
        evaluation.evaluateModel(randomForest, cacheManagement.getTestDataset(sessionId));

        return buildClassificationResponse(evaluation);
    }
    public ClassificationResponse performSupportVectorMachines(String sessionId, Request request) throws Exception {

        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());

        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));

        SMO smo = new SMO();
        smo.buildClassifier(trainDataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        Evaluation evaluation = new Evaluation(trainDataset);
        evaluation.evaluateModel(smo, cacheManagement.getTestDataset(sessionId));

        return buildClassificationResponse(evaluation);
    }


    public ClassificationResponse doClassification(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        switch (request.getRegressionType()) {
            case "NaiveByes":
                return performNaiveBayesClassification(sessionId, request);
            case "DecisionTrees":
                return performDecisionTreeJ48(sessionId, request);
            case "Logistic":
                return performLogistic(sessionId, request);
            case "kNearestNeighbors":
                return performKNearestNeighbors(sessionId, request);
            case "RandomForest":
                return performRandomForest(sessionId, request);
            case "SupportVectorMachines":
                return performSupportVectorMachines(sessionId, request);

            default:
                throw new BaseRunTimeException(400, "Specified algorithm " + request.getRegressionType() + " not found.");
        }
    }

    private void prepareTrainTestDataset(Request request, String sessionId) {
        UserSession userSession = cacheManagement.getUserSession(sessionId);
        Instances dataset = userSession.getDataset();

        int trainDateRecords = Math.round ((dataset.size() * request.getTrainDataSize())/100);

        userSession.setTrainDataset(new Instances(dataset, 0, trainDateRecords));
        userSession.setTestDataset(new Instances(dataset, trainDateRecords, (dataset.size()-trainDateRecords -1)));
    }

    private ClassificationResponse buildClassificationResponse(Evaluation evaluation) throws Exception {

        return ClassificationResponse.builder()
                //.areaUnderRoc(evaluation.areaUnderROC(1))
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

    public RegressionResponse performLinearRegression(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());
        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));

        LinearRegression regression = new LinearRegression();
        regression.setMinimal(false);
        regression.setOutputAdditionalStats(true);
        regression.buildClassifier(trainDataset);

        Evaluation evaluation = new Evaluation(trainDataset);


        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(regression, testDataset);
        double[] coefficients = regression.coefficients();

        boolean[] selectedAttr = regression.getM_SelectedAttributes();
        List<String> list = new ArrayList<>();
        for(int i=0; i<trainDataset.numAttributes(); i++) {
            if(selectedAttr[i]) {
                list.add(trainDataset.attribute(i).name());
            }
        }

        RegressionResponse regressionResponse = prepareRegressionResponse(trainDataset, evaluation);
        regressionResponse.setCoefficients(regression.getM_Coefficients());
        regressionResponse.setFStat(regression.getM_FStat());
        regressionResponse.setRSquared(regression.getM_RSquared());
        regressionResponse.setRSquaredAdj(regression.getM_RSquaredAdj());
        regressionResponse.setTStats(regression.getM_TStats());
        regression.getM_SelectedAttributes();
        regressionResponse.setSelectedAttributeNames(list);
        regressionResponse.setStdErrorOfCoefficient(regression.getM_StdErrorOfCoef());
        return regressionResponse;
    }


    public RegressionResponse performRandomForestRegression(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());
        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));

        RandomForest regression = new RandomForest();
        regression.setOutputOutOfBagComplexityStatistics(true);
        regression.buildClassifier(trainDataset);

        Evaluation evaluation = new Evaluation(trainDataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(regression, testDataset);

        return prepareRegressionResponse(trainDataset, evaluation);
    }


    public RegressionResponse performMultilayerPerceptron(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());
        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));

        MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
        multilayerPerceptron.buildClassifier(trainDataset);

        Evaluation evaluation = new Evaluation(trainDataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(multilayerPerceptron, testDataset);

        return prepareRegressionResponse(trainDataset, evaluation);
    }

    public RegressionResponse performSupportVectorRegression(String sessionId, Request request) throws Exception {

/*        AbstractClassifier classifier = ( AbstractClassifier ) Class.forName("weka.classifiers.functions.LibSVM" ).newInstance();
        classifier.buildClassifier();*/
        return null;
    }

    public RegressionResponse performDecisionTrees(String sessionId, Request request) {
        return null;
    }

    public RegressionResponse performM5P(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Attribute classAttribute = trainDataset.attribute(request.getYColumn());
        trainDataset.setClassIndex(getClassIndex(trainDataset, request.getYColumn()));

        M5P m5P = new M5P();
        m5P.buildClassifier(trainDataset);

        Evaluation evaluation = new Evaluation(trainDataset);

        Instances testDataset = cacheManagement.getTestDataset(sessionId);
        testDataset.setClassIndex(getClassIndex(testDataset, request.getYColumn()));
        evaluation.evaluateModel(m5P, testDataset);

        return prepareRegressionResponse(trainDataset, evaluation);
    }

    public RegressionResponse doRegression(String sessionId, Request request) throws Exception {

        switch (request.getRegressionType()) {
            case "LinearRegression" :
                return performLinearRegression(sessionId, request);
            case "RandomForest":
                return performRandomForestRegression(sessionId, request);
            case "MultilayerPerceptron":
                return performMultilayerPerceptron(sessionId, request);
            case "SupportVectorRegression":
                return performSupportVectorRegression(sessionId, request);
            case "DecisionTrees":
                return performDecisionTrees(sessionId, request);
            case "M5P":
                return performM5P(sessionId, request);
        }

        throw new BaseRunTimeException(404, "Specified algorithm not found.");
    }



    private RegressionResponse prepareRegressionResponse(Instances dataset, Evaluation evaluation) throws Exception {

        return RegressionResponse.builder()
                .correlationCoefficient(evaluation.correlationCoefficient())
                .meanAbsoluteError(evaluation.meanAbsoluteError())
                .relativeAbsoluteError(evaluation.relativeAbsoluteError())
                .totalNumberOfInstance(dataset.numInstances())
                .summary(evaluation.toSummaryString())
                .build();
    }


    public Map<String, AttributeStatistic> getMissingValues(String sessionId)  {
        Instances dataset = cacheManagement.getDatasource(sessionId);

        if(dataset.classIndex() == -1) {
            dataset.setClassIndex(dataset.numAttributes()-1);
        }

        Map<String, AttributeStatistic> missingValueMap = new HashMap<>();

        int numAttr = dataset.numAttributes() - 1;
        List<AttributeStatistic> attributeList = new ArrayList<>(numAttr);

        for(int i=0; i<numAttr; i++) {
            Attribute attribute = dataset.attribute(i);
            AttributeStats as = dataset.attributeStats(i);

            if(as.missingCount>0) {

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

                missingValueMap.put(attribute.name(), atrSt);
            }
        }

        return missingValueMap;
    }

    public void handleNominalValues(String sessionId, String attributeName) {

        Instances dataset = cacheManagement.getDatasource(sessionId);

        int num = dataset.numAttributes();
        Instance instance = dataset.instance(0);
        String str = "";
        prepareNominalValueMap(sessionId, attributeName);

        UserSession userSession = cacheManagement.getUserSession(sessionId);
        Map<String, Map> nominalValueMap = userSession.getNominalValueMap();
        Iterator<String> iterator = nominalValueMap.keySet().iterator();

        addAttribute(sessionId, attributeName+"-code");

        dataset.parallelStream().forEach(inst -> {
            Iterator keySetIt =  nominalValueMap.keySet().iterator();

            int numAttr = inst.numAttributes();
            Attribute sourceAttrib = inst.attribute(dataset.attribute(attributeName).index());
            Attribute targetAttrib = inst.attribute(dataset.attribute(attributeName+"-code").index());
            inst.setValue(targetAttrib, Integer.parseInt(nominalValueMap.get(attributeName).get(inst.stringValue(sourceAttrib)).toString()));
        });
    }


    private Attribute getAttributeFromInstance(Instance instance, String attribName) {
        int numAttr = instance.numAttributes();
        for(int i=0; i<numAttr; i++) {
            if(instance.attribute(i).name().equalsIgnoreCase(attribName)) {
                return instance.attribute(i);
            }
        }

        throw new BaseRunTimeException(404, "Attribute not found.");
    }

    private void prepareNominalValueMap(String sessionId, String attributeName) {
        UserSession userSession = cacheManagement.getUserSession(sessionId);
        Instances dataset = userSession.getDataset();

        int num = dataset.numAttributes();
        Map<String, ConcurrentMap<?, Integer>> nominalValueMap = new HashMap<>();

        Attribute attribute = dataset.attribute(attributeName);
        if(attribute.isNominal()) {
            ConcurrentMap conMap = getUniqueValuesWithCount(sessionId, attribute.name());

            Iterator it = conMap.keySet().iterator();
            int code = 0;
            while(it.hasNext()) {
                Object obj = it.next();

                conMap.put(obj, code);
                code++;
            }

            userSession.getNominalValueMap().put(attribute.name(), conMap);
            nominalValueMap.put(attribute.name(), getUniqueValuesWithCount(sessionId,attribute.name()));
        }
    }


    public List<Map> getTopRecs(String sessionId, int noOfRec) {
        Instances dataset = cacheManagement.getDatasource(sessionId);

        List<Map> instanceList = new ArrayList<>(noOfRec);
        int total = dataset.size();

        ObjectMapper mapper = new ObjectMapper();

        for(int j=0; j<noOfRec; j++) {
            Instance instance = dataset.get(j);

            int numattr = instance.numAttributes();
            Map<String, Object> attributeMap = new HashMap<>();

            for(int i=0; i<numattr; i++) {
                Attribute attribute = instance.attribute(i);
                attributeMap.put(attribute.name(), attribute.isNumeric()?instance.value(attribute):instance.stringValue(attribute));
            }
            instanceList.add(attributeMap);
        }


        return instanceList;
    }

    public List<Instance> getTrailRecs(String sessionId, int noOfRec) {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        List<Instance> instanceList = new ArrayList<>(noOfRec);
        int total = dataset.size();
        int i = total - noOfRec;

        for(;(i<noOfRec && i<total); i++) {
            instanceList.add(dataset.get(i));
        }

        return instanceList;
    }

    public void addAttribute(String sessionId, String attributeName) {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        Attribute attribute = new Attribute(attributeName);
        dataset.insertAttributeAt(attribute, dataset.numAttributes());
    }

    public void deleteAttribute(String sessionId, String attributeName) {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        Attribute attribute = dataset.attribute(attributeName);
        dataset.deleteAttributeAt(attribute.index());
    }

    public Map<String, Integer> getOutliers(String sessionId) throws Exception {
        Instances dataset = cacheManagement.getDatasource(sessionId);

        InterquartileRange interquartileRange = new InterquartileRange();
        Instances icopy = new Instances(dataset); //, 0, 1);

        interquartileRange.setInputFormat(icopy);
        interquartileRange.setOptions(Constants.INTER_QUARTILE_OPTION);

        Map<String, Integer> outlierCountMap = new HashMap<>();
        List<Attribute> attributeNames = new ArrayList<>();

        Instances filterDataset = Filter.useFilter(icopy, interquartileRange);
        for(int i=0;i<filterDataset.numAttributes(); i++) {
            if(filterDataset.attribute(i).name().contains("_Outlier")) {
                attributeNames.add(filterDataset.attribute(i));
                outlierCountMap.put(filterDataset.attribute(i).name(), 0);
            }
        }

        filterDataset.stream().parallel().forEach(instance -> {
            attributeNames.stream().forEach(attr->{

                if(instance.stringValue(attr).equals("yes")) {
                    outlierCountMap.put(attr.name(), (outlierCountMap.get(attr.name()).intValue()+1));
                }
            });
        });

        return outlierCountMap;
    }






    public Map<String, Object> getDataset(String sessionId) throws Exception {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        int noOfRec = dataset.numInstances();

        List<Map> instanceList = new ArrayList<>(dataset.numInstances());
        int total = dataset.size();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();

        for(int j=0; j<noOfRec; j++) {
            Instance instance = dataset.get(j);

            int numAttr = instance.numAttributes();
            Map<String, Object> attributeMap = new HashMap<>();

            for(int i=0; i<numAttr; i++) {
                if(dataset.attribute(i).isNumeric()) {
                    responseMap.put(dataset.attribute(i).name(), dataset.attributeToDoubleArray(i)) ;
                }
            }
        }

        return responseMap;
    }

    public List<Map<String, Object>> convertNominalToBinary(String sessionId, String classAttribute) throws Exception {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        dataset.setClassIndex(getClassIndex(dataset, classAttribute));
        String[] remoteOpt = new String[]{ "-N", "-spread-attribute-weight"};


        NominalToBinary nominalToBinary = new NominalToBinary();
        nominalToBinary.setBinaryAttributesNominal(false);
        nominalToBinary.setOptions(remoteOpt);
        nominalToBinary.setInputFormat(dataset);


        StringToNominal stringToNominal = new StringToNominal();

        Instances filterDataset = Filter.useFilter(dataset, nominalToBinary);
        cacheManagement.updateDataset(sessionId, filterDataset);

        int numAttr = filterDataset.numAttributes();

        List<Attribute> attributes = new ArrayList<>();
        for(int i=0; i<numAttr; i++) {
            attributes.add(filterDataset.attribute(i));
        }

        return filterDataset.stream().parallel().map(instance -> {
            Map<String, Object> valueMap = new HashMap<>();
            attributes.stream().forEach(attribute -> {
                if(attribute.isNumeric()) {
                    valueMap.put(attribute.name(), instance.value(attribute));
                } else {
                    valueMap.put(attribute.name(), instance.stringValue(attribute));
                }
            });

            return valueMap;
        }).collect(Collectors.toList());
    }

    private String performApriori(String sessionId, Request request) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Instances testDataset = cacheManagement.getTestDataset(sessionId);

/*
        Discretize discretize = new Discretize();
        String[] options = new String[4];
        options[0] = "-R"; // Range of variables to Discretize
        options[1] = "first-last"; // All variables will be Discretized
        options[2] = "-precision"; // Number of bins to create
        options[3] = "5"; // Here we create 5 bins
        discretize.setOptions(options);
        discretize.setInputFormat(trainDataset);

        Instances filterDataset = Filter.useFilter(trainDataset, discretize);
*/


        NominalToBinary nominalToBinary = new NominalToBinary();

        nominalToBinary.setInputFormat(trainDataset);
        Instances filterDataset = Filter.useFilter(trainDataset, nominalToBinary);




        Apriori apriori = new Apriori();
        apriori.setClassIndex(getClassIndex(filterDataset, request.getYColumn()));
        apriori.buildAssociations(filterDataset);

        AssociatorEvaluation evaluation = new AssociatorEvaluation();
        String evaluationString = evaluation.evaluate(apriori, Filter.useFilter(testDataset, nominalToBinary));

        return apriori.toString() + "\n Evaluation: \n" + evaluationString;
    }


    private String performFPGrowth(String sessionId) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Instances testDataset = cacheManagement.getTestDataset(sessionId);

        FPGrowth fpGrowth = new FPGrowth();
        fpGrowth.buildAssociations(trainDataset);

        AssociatorEvaluation evaluation = new AssociatorEvaluation();
        String evaluationString = evaluation.evaluate(fpGrowth, testDataset);

        return fpGrowth.toString() + "\n Evaluation: \n" + evaluationString;
    }

    public String doAssociation(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        switch (request.getRegressionType()) {
            case "Apriori":
                return performApriori(sessionId, request);
            case "FPGrowth":
                return performFPGrowth(sessionId);

            default:
                throw new BaseRunTimeException(400, "Specified algorithm " + request.getRegressionType() + " not found.");
        }
    }

    private String performCanopy(String sessionId) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Instances testDataset = cacheManagement.getTestDataset(sessionId);

        Canopy canopy = new Canopy();
        canopy.buildClusterer(trainDataset);

        ClusterEvaluation evaluation = new ClusterEvaluation();
        evaluation.setClusterer(canopy);
        evaluation.evaluateClusterer(testDataset);

        return canopy.toString() + "\n Evaluation: \n" + evaluation.toString();
    }

    private String performCobweb(String sessionId) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Instances testDataset = cacheManagement.getTestDataset(sessionId);

        Cobweb cobweb = new Cobweb();
        cobweb.buildClusterer(trainDataset);

        ClusterEvaluation evaluation = new ClusterEvaluation();
        evaluation.setClusterer(cobweb);
        evaluation.evaluateClusterer(testDataset);

        return cobweb.toString() + "\n Evaluation: \n" + evaluation.toString();
    }

    private String performSimpleKMean(String sessionId) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Instances testDataset = cacheManagement.getTestDataset(sessionId);

        SimpleKMeans simpleKMeans = new SimpleKMeans();
        simpleKMeans.buildClusterer(trainDataset);

        ClusterEvaluation evaluation = new ClusterEvaluation();
        evaluation.setClusterer(simpleKMeans);
        evaluation.evaluateClusterer(testDataset);

        return simpleKMeans.toString() + "\n Evaluation: \n" + evaluation.toString();
    }

    private String performFarthestFirst(String sessionId) throws Exception {
        Instances trainDataset = cacheManagement.getTrainDataset(sessionId);
        Instances testDataset = cacheManagement.getTestDataset(sessionId);

        FarthestFirst farthestFirst = new FarthestFirst();
        farthestFirst.buildClusterer(trainDataset);

        ClusterEvaluation evaluation = new ClusterEvaluation();
        evaluation.setClusterer(farthestFirst);
        evaluation.evaluateClusterer(testDataset);

        return farthestFirst.toString() + "\n Evaluation: \n" + evaluation.toString();
    }

    public String doCluster(String sessionId, Request request) throws Exception {
        prepareTrainTestDataset(request, sessionId);
        switch (request.getRegressionType()) {
            case "Canopy":
                return performCanopy(sessionId);
            case "Cobweb":
                return performCobweb(sessionId);
            case "SimpleKMean":
                return performSimpleKMean(sessionId);
            case "FarthestFirst(":
                return performFarthestFirst(sessionId);
            default:
                throw new BaseRunTimeException(400, "Specified algorithm " + request.getRegressionType() + " not found.");
        }
    }


    public List<String> getPredictiveAbility(String sessionId, String classAttribute) throws Exception {
        Instances dataset = cacheManagement.getDatasource(sessionId);

        dataset.setClassIndex(getClassIndex(dataset, classAttribute));

        AttributeSelection filter = new AttributeSelection();

        CfsSubsetEval evaluation = new CfsSubsetEval();
        evaluation.setMissingSeparate(true);
        evaluation.setLocallyPredictive(true);
        evaluation.setPreComputeCorrelationMatrix(true);
        evaluation.setDoNotCheckCapabilities(false);

        GreedyStepwise search = new GreedyStepwise();
        search.setGenerateRanking(true);

        search.setSearchBackwards(true);
        filter.setEvaluator(evaluation);
        filter.setSearch(search);
        filter.setInputFormat(dataset);

        Instances newDataset = Filter.useFilter(dataset, filter);
        int numAttr = newDataset.numAttributes();
        List<String> listAttribute = new ArrayList<>();

        for(int i=0; i<numAttr; i++) {
            listAttribute.add(newDataset.attribute(i).name());
        }

        log.info("cfsSubsetEval ======================");
        log.info(filter.toString());
        log.info(search.toString());
        log.info(evaluation.toString());

        return listAttribute;
    }



    public double[][] getCorrelation(String sessionId, String classAttribute) throws Exception {
        Instances dataset = cacheManagement.getDatasource(sessionId);
        dataset.setClassIndex(getClassIndex(dataset, classAttribute));

        AttributeSelection filter = new AttributeSelection();
        PrincipalComponents evaluator = new PrincipalComponents();

        Ranker search = new Ranker();
        search.setGenerateRanking(true);

        filter.setSearch(search);
        filter.setEvaluator(evaluator);
        filter.setInputFormat(dataset);

        Instances newInstances = Filter.useFilter(dataset, filter);
        double[][] correlationMatrix = evaluator.getCorrelationMatrix();

        for(int i=0; i<correlationMatrix.length; i++) {
            correlationMatrix[i][i] = 1.000;
        }

        return correlationMatrix;
    }
}
