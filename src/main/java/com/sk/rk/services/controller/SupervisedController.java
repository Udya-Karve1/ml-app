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
import weka.core.Instance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


@RestController
@RequestMapping("/v1/ml-api")
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

    @GetMapping("/missing-values")
    @Operation(summary = "Get dataset stats", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<Map<String, AttributeStatistic>> getMissingValues(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
    ) throws Exception {
        return new ResponseEntity<>(mlService.getMissingValues(sessionId), HttpStatus.OK);
    }

    @GetMapping("/top/{no}")
    @Operation(summary = "Get dataset stats", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<List<Map>> getTopRec(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @PathVariable("no") Integer no
    ) throws Exception {
        return new ResponseEntity<>(mlService.getTopRecs(sessionId, no), HttpStatus.OK);
    }

    @GetMapping("/trail/{no}")
    @Operation(summary = "Get dataset stats", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<List<Instance>> getTrailRec(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @PathVariable("no") Integer no
    ) throws Exception {
        return new ResponseEntity<>(mlService.getTrailRecs(sessionId, no), HttpStatus.OK);
    }

    @PatchMapping("/add-attribute/{attribute-name}")
    @Operation(summary = "Add attribute to dataset", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<String> addAttribute(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @PathVariable("attribute-name") String name
    ) throws Exception {
        mlService.addAttribute(sessionId, name);
        return new ResponseEntity<>("Attribute added.", HttpStatus.OK);
    }

    @PutMapping("/handle-nominal/{attribute-name}")
    @Operation(summary = "Add attribute to dataset", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<String> handleNominal(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @PathVariable("attribute-name") String name
    ) throws Exception {
        mlService.handleNominalValues(sessionId, name);
        return new ResponseEntity<>("Attribute updated.", HttpStatus.OK);
    }


    @DeleteMapping("/delete-attribute/{attribute-name}")
    @Operation(summary = "Add attribute to dataset", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<String> deleteAttribute(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
            , @PathVariable("attribute-name") String name
    ) throws Exception {
        mlService.deleteAttribute(sessionId, name);
        return new ResponseEntity<>("Attribute deleted.", HttpStatus.OK);
    }


    @GetMapping("/outliers")
    @Operation(summary = "Get outliers", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<Map<String, Integer>> getOutliers(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
    ) throws Exception {
        return new ResponseEntity<>(mlService.getOutliers(sessionId), HttpStatus.OK);
    }

    @GetMapping("/data-set")
    @Operation(summary = "Get outliers", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<Map<String, Object>> getDataset(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
    ) throws Exception {
        return new ResponseEntity<>(mlService.getDataset(sessionId), HttpStatus.OK);
    }

    @PutMapping("/handle-categorical")
    @Operation(summary = "Handle nominal", parameters = {
            @Parameter(name = Constants.USER_SESSION, in = ParameterIn.HEADER)
    })
    public ResponseEntity<List<Map<String, Object>>> handleNominal(
            @RequestHeader(value = Constants.USER_SESSION) String sessionId
    ) throws Exception {
        return new ResponseEntity<>(mlService.convertNominalToBinary(sessionId), HttpStatus.OK);
    }
}
