package com.sk.rk.services.service;

import com.sk.rk.services.model.UserSession;
import org.springframework.stereotype.Service;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class CacheManagement {


    protected static final Map<String, UserSession> cacheMap = new HashMap<>();

    public UserSession createUserSession(String filePath, String fileName, String uploadedFileName) throws Exception {
        UserSession userSession = new UserSession(filePath);
        userSession.setDataset(prepareDatasource(filePath));
        userSession.setFileName(fileName);
        userSession.setUploadedFileName(uploadedFileName);
        cacheMap.put(userSession.getSessionId(), userSession);

        return userSession;
    }

    public UserSession createUserSession(String filePath) throws Exception {
        UserSession userSession = new UserSession(filePath);
        userSession.setDataset(prepareDatasource(filePath));
        cacheMap.put(userSession.getSessionId(), userSession);

        return userSession;
    }

    public UserSession getUserSession(String sessionId) {
        UserSession userSession = cacheMap.get(sessionId);
        userSession.setLastReferred(System.currentTimeMillis());

        cacheMap.put(userSession.getSessionId(), userSession);

        return userSession;
    }

    public String getFileNameBySessionId(String sessionId) {
        return getUserSession(sessionId.toString()).getDataSetFilePath();
    }

    public Instances getDatasource(String sessionId) {
        return getUserSession(sessionId).getDataset();
    }

    private Instances prepareDatasource(String filePath) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(filePath);
        return source.getDataSet();
    }

    public Instances getTrainDataset(String sessionId) {
        UserSession session = getUserSession(sessionId);
        return session.getTrainDataset();
    }

    public Instances getTestDataset(String sessionId) {
        UserSession session = getUserSession(sessionId);
        return session.getTestDataset();
    }

    public void updateDataset(String sessionId, Instances instances) {
        UserSession userSession = getUserSession(sessionId);
        userSession.setDataset(instances);
    }

    public boolean isNominalAttribute(String sessionId) {
        Instances instances = getDatasource(sessionId);
        int numAttr = instances.numAttributes();
        for(int i=0; i<numAttr; i++) {
            if(instances.attribute(i).isNominal())
                return true;
        }


        return false;
    }

}
