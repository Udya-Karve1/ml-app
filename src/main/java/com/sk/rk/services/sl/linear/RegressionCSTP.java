package com.sk.rk.services.sl.linear;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegressionCSTP {
	private String fieldName;
	private Double coefficient;
	private Double standardError;
	private Double tStat;
	private Double pValue;
}
