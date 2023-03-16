package com.sk.rk.services.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class LinearRegressionRequest implements Serializable {
    private String yColumn;
    private String[] xColumns;
    private String tableName;
    private Integer trainedData;
}
