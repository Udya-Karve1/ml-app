package com.sk.rk.services.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import weka.core.Instances;

import java.util.UUID;

public class UserSession {
    private String sessionId;
    private String originalDataSetFilePath;
    private String dataSetFilePath;

    @JsonIgnore
    private Instances dateset;

    @JsonIgnore
    private long lastReferred;


    private Instances trainDataset;
    private Instances testDataset;



    public UserSession(String dataSetFilePath) {
        this.originalDataSetFilePath = dataSetFilePath;
        this.sessionId = UUID.randomUUID().toString();
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

    public Instances getDateset() {
        return dateset;
    }

    public void setDateset(Instances dateset) {
        this.dateset = dateset;
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
}
