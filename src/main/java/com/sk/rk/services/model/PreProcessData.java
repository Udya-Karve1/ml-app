package com.sk.rk.services.model;

import lombok.Data;
import weka.core.Instance;

@Data
public class PreProcessData {
    private Instance trainDataset;
    private Instance testDataset;
}
