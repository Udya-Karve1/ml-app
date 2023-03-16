package com.sk.rk.services.model;

import lombok.Data;

import java.util.List;

@Data
public class Request {
    private Integer trainDataSize;
    private String yColumn;
    private List<String> xColumns;
    private String regressionType;
}
