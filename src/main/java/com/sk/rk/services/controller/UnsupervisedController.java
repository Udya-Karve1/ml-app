package com.sk.rk.services.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/un-supervised")
public class UnsupervisedController {

    @PostMapping("/clustering")
    @Operation(summary = "clustering")
    public ResponseEntity<String> doClustering(
            @RequestParam("classification-type") String classificationType
    ) {
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }
}
