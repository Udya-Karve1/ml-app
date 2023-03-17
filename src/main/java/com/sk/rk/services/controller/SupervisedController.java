package com.sk.rk.services.controller;

import com.sk.rk.services.model.*;
import com.sk.rk.services.service.MLService;
import com.sk.rk.services.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


@RestController
@RequestMapping("/v1/supervised")
public class SupervisedController {

    @Autowired
    private MLService mlService;

    @PostMapping("/data-set/{file-name}")
    @Operation(summary = "Upload CSV file.")
    public ResponseEntity<UserSession> postDateset(
            @PathVariable("file-name") String fileName
            , @RequestPart("file") MultipartFile file
    ) throws Exception {
        return new ResponseEntity<>(mlService.uploadCSV(file), HttpStatus.CREATED);
    }

    @PostMapping("/regression")
    @Operation(summary = "Regression", parameters = {
            //@Parameter(name = "regression-type", schema = @Schema(allowableValues = {"Liner Regression", "Random Forest", "KNN Model","Support Vector Machines","Gausian Regression","Polynomial Regression"})),
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<RegressionResponse> linerRegression(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @RequestBody Request request
            ) throws Throwable {

        return new ResponseEntity<>(mlService.doRegression(sessionId, request), HttpStatus.OK);
    }

    @PostMapping("/classification")
    @Operation(summary = "Classification", parameters = {
            //@Parameter(name = "classification-type", schema = @Schema(allowableValues = {"Logistic Regression","Decision Tree","Random forest","Support vector machine","K-nearest neighbour","Naive bayes"})),
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<ClassificationResponse> doClassification(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @RequestBody Request request
    ) throws Exception {

        return new ResponseEntity<>(mlService.doClassification(sessionId, request), HttpStatus.OK);
    }


    @GetMapping("/stats")
    @Operation(summary = "Get dataset stats", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<List<AttributeStatistic>> getDatasetStat(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
    ) throws Exception {
        return new ResponseEntity<>(mlService.getAttributeStat(sessionId), HttpStatus.OK);
    }

    @GetMapping("/unique-values/{field-name}")
    @Operation(summary = "Get dataset stats", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<ConcurrentMap> getUniqueValue(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @PathVariable("field-name") String fieldName
    ) throws Exception {
        mlService.getUniqueValuesWithCount(sessionId, fieldName);
        return new ResponseEntity<>(mlService.getUniqueValuesWithCount(sessionId, fieldName), HttpStatus.OK);
    }
}
