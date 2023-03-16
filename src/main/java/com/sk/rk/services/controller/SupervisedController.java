package com.sk.rk.services.controller;

import com.sk.rk.services.model.AttributeStatistic;
import com.sk.rk.services.model.Request;
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

import java.util.List;


@RestController
@RequestMapping("/v1/supervised")
public class SupervisedController {

    @Autowired
    private MLService mlService;

    @PostMapping("/data-set/{file-name}")
    @Operation(summary = "Upload CSV file.")
    public ResponseEntity<String> postDateset(
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
    public ResponseEntity<String> linerRegression(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @RequestBody Request request
            ) throws Throwable {

        mlService.prepareLinearRegressionModel();
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

    @PostMapping("/classification")
    @Operation(summary = "Classification", parameters = {
            //@Parameter(name = "classification-type", schema = @Schema(allowableValues = {"Logistic Regression","Decision Tree","Random forest","Support vector machine","K-nearest neighbour","Naive bayes"})),
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<String> doClassification(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @RequestBody Request request
    ) throws Exception {
        mlService.doClassification(sessionId, request);
        return new ResponseEntity<>("Ok", HttpStatus.OK);
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
}
