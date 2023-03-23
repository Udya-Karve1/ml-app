package com.sk.rk.services.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserSession {
    private String sessionId;
    private String originalDataSetFilePath;
    private String dataSetFilePath;
    private String fileName;
    private String uploadedFileName;

    @JsonIgnore
    private Instances dataset;

    @JsonIgnore
    private long lastReferred;


    private Instances trainDataset;
    private Instances testDataset;

    private Map<String, Map> nominalValueMap = new HashMap<>();



    public UserSession(String dataSetFilePath) {
        this.originalDataSetFilePath = dataSetFilePath;
        this.sessionId = UUID.randomUUID().toString().toUpperCase();
        this.lastReferred = System.currentTimeMillis();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getOriginalDataSetFilePath() {
        return originalDataSetFilePath;
    }

    public void setOriginalDataSetFilePath(String originalDataSetFilePath) {
        this.originalDataSetFilePath = originalDataSetFilePath;
    }

    public String getDataSetFilePath() {
        if(StringUtils.isBlank(this.dataSetFilePath)) {
            return this.originalDataSetFilePath;
        }
        return dataSetFilePath;
    }

    public void setDataSetFilePath(String dataSetFilePath) {
        this.dataSetFilePath = dataSetFilePath;
    }

    public long getLastReferred() {
        return lastReferred;
    }

    public void setLastReferred(long lastReferred) {
        this.lastReferred = lastReferred;
    }

    public Instances getTrainDataset() {
        return trainDataset;
    }

    public void setTrainDataset(Instances trainDataset) {
        this.trainDataset = trainDataset;
    }

    public Instances getTestDataset() {
        return testDataset;
    }

    public void setTestDataset(Instances testDataset) {
        this.testDataset = testDataset;
    }

    public Map<String, Map> getNominalValueMap() {
        return nominalValueMap;
    }
    public void setNominalValueMap(Map<String, Map> nominalValueMap) {
        this.nominalValueMap = nominalValueMap;
    }

    public Instances getDataset() {
        return dataset;
    }

    public void setDataset(Instances dataset) {
        this.dataset = dataset;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }
}
