package com.sk.rk.services.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.sk.rk.services.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

@Slf4j
public class CsvService {

    Map<String, String> headerTypeMap = new HashMap<>();
    List<Map<String, String>> columnTypeList = new ArrayList<>();
    Map<String, Integer> headerIndexTypeMap = new HashMap<>();
    Map<Integer, String> headerReverseIndexTypeMap = new HashMap<>();
    Map<String, String> headerNonStringypeMap = new HashMap<>();

    List<String[]> list = null;
    List<Integer> nonStringIndexes = null;
    private final String filePath;
    private File savedFile;

    ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor has one string parameter a path to file
     * Based on this CSV reader reads the file and prepare
     * list of arrays of string for each values of file
     *
     * @param filePath
     * @throws IOException
     * @throws CsvException
     */
    /*public CsvService(String filePath, File csvFile) throws IOException, CsvException {
        this.filePath = filePath;
        this.savedFile = csvFile;
        initObjects();
            try(CSVReader reader = new CSVReader(new FileReader(this.savedFile));) {
            list = reader.readAll();
        } catch (IOException | CsvException e) {
            log.error("Exception :{} ",e);
            throw e;
        }
    }*/

    public CsvService(String filePath, InputStream in) throws IOException, CsvException {
        this.filePath = filePath;
        //this.savedFile = csvFile;

        Reader inReader = new InputStreamReader(in);

        initObjects();
        try(CSVReader reader = new CSVReader(inReader);) {
            list = reader.readAll();
        } catch (IOException | CsvException e) {
            log.error("Exception :{} ",e);
            throw e;
        }
    }

    /**
     * Initiallize required data structurs to work on
     */
    private void initObjects() {

        headerTypeMap = new HashMap<>();
        headerIndexTypeMap = new HashMap<>();
        headerReverseIndexTypeMap = new HashMap<>();
        headerNonStringypeMap = new HashMap<>();
        nonStringIndexes = new ArrayList<>();
    }

    public String geDataTypes() throws JsonProcessingException {
        return mapper.writeValueAsString(columnTypeList);
    }

    public List<Map<String, String>> getColumnList() {
        return this.columnTypeList;
    }

    /**
     * Process List of array to prepare JSON out of it.
     * Scan values and datatypes and then prepare Map collections
     * with required data type and value
     *
     * @return
     * @throws JsonProcessingException
     */
    public String getStringFormatValues() throws JsonProcessingException {

        long start = System.currentTimeMillis();

        prepareCSVHeaderType(list);
        validateDataType(list, list.get(0));
        String output = mapper.writeValueAsString(prepareValueMapList());

        long end = System.currentTimeMillis();
        log.debug("Total time taken to process: {}", (end-start));

        return output;
    }

    public List<Map<String, Object>> getCSVValues() throws JsonProcessingException {

        long start = System.currentTimeMillis();

        prepareCSVHeaderType(list);
        validateDataType(list, list.get(0));
        List<Map<String, Object>> csvData = prepareValueMapList();

        long end = System.currentTimeMillis();
        log.debug("Total time taken to process: {}", (end-start));

        return csvData;
    }

    /**
     * Method prepares header values to be used as column names in table.
     * All special chars, while space are replaced by underscore(_).
     *
     * @param list
     */
    public void prepareCSVHeaderType(List<String[]> list) {

        String[] headers = list.get(0);
        String[] firstRow = list.get(1);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i<headers.length; i++) {
            headerIndexTypeMap.put(headers[i], Integer.valueOf(i));
            nonStringIndexes.add(Integer.valueOf(i));

            if(StringUtils.isNotBlank(firstRow[i])) {
                getDataTypeOfValue(firstRow[i], headers[i], i);
            } else {
                prepareAllHeadermap(headers[i], CSVUtils.NULL, i);
            }
        }

        long endTime = System.currentTimeMillis();
        log.debug("Total time: {}", (endTime-startTime));
    }


    /**
     * Method identify datatype of given value.
     *
     * @param value
     * @param header
     * @param index
     */
    private void getDataTypeOfValue(String value, String header, Integer index) {
        if(CSVUtils.isIntegerType(value)) {
            prepareAllHeadermap(header, CSVUtils.INTEGER, index);
        } else if(CSVUtils.isDoubleType(value)) {
            prepareAllHeadermap(header, CSVUtils.FLOAT, index);
        }else if (CSVUtils.isTime(value)){
            prepareAllHeadermap(header, CSVUtils.TIME, index);
        }else if (CSVUtils.isDate(value)){
            prepareAllHeadermap(header, CSVUtils.DATE, index);
        }else if (CSVUtils.isDateTime(value)){
            prepareAllHeadermap(header, CSVUtils.DATE_TIME, index);
        }else if (CSVUtils.isPercentage(value)){
            prepareAllHeadermap(header, CSVUtils.PERCENTAGE, index);
        } else {
            prepareAllHeadermap(header, CSVUtils.VARCHAR, index);
            nonStringIndexes.remove(index);
        }
    }

    private void validateNonBlankNonNullDataType(String value, String header, Integer index) {
        if(StringUtils.isNotBlank(value)) {

            if(isIntegerWithNonIntegerValue(value, header)) {
                changeTypeFromInteger(header, value, index);
            } else if(isFloatWithNonFloatValue(value, header)) {
                prepareAllHeadermap(header, CSVUtils.VARCHAR, index);
            }else if (isTimeWithNonTimeValue(value, header)){
                changeTypeFromTime(header, value, index);
            }else if (isDateWithNonDateValue(value, header)){
                changeTypeFromDate(header, value, index);
            }else if (isDatetimeWithNonDatetimeValue(value, header)){
                prepareAllHeadermap(header, CSVUtils.VARCHAR, index);
            }else if (isPercentageWithNonPercentageValue(value, header)){
                prepareAllHeadermap(header, CSVUtils.VARCHAR, index);
            } else if(headerTypeMap.get(header).equals(CSVUtils.NULL)) {
                getDataTypeOfValue(value, header, index);
            }
        } else {
            if(headerTypeMap.get(header).equals(CSVUtils.NULL))
                prepareAllHeadermap(header, CSVUtils.NULL, index);
        }
    }

    public void validateDataType(List<String[]> list, String[] headers) {

        for (int i = 2; i<list.size(); i++) {

            String[] row = list.get(i);

            for (Integer index: nonStringIndexes) {
                validateNonBlankNonNullDataType(row[index], headers[index], index);
            }
        }

        setVarcharForNull();
    }

    private String[] prepareStringArrayOfHeader(String[] headers) {

        Set<String> headerSet = new HashSet<>();

        for (int i = 0; i< headers.length; i++) {
            String newValue = getHeaderValue(headerSet, headers[i]);
            headerSet.add(newValue);
            columnTypeList.add(createDataTypeObject(headers[i], newValue));
            headers[i] = newValue;
        }

        return headers;
    }

    private Map<String, String> createDataTypeObject(String oldValue, String newValue) {
        Map<String, String> dataTypeMap = new HashMap<>();
        dataTypeMap.put(CSVUtils.COLUMN_NAME, newValue);
        if(headerTypeMap.get(oldValue).equals(CSVUtils.PERCENTAGE)) {
            dataTypeMap.put(CSVUtils.DATA_TYPE, CSVUtils.FLOAT);
        } else {
            dataTypeMap.put(CSVUtils.DATA_TYPE, headerTypeMap.get(oldValue));
        }


        return dataTypeMap;
    }

    private String getHeaderValue(Set<String> set, String value) {

        value = CSVUtils.replaceSpecialChars(value);
        String tmpValue = value;
        int index = 1;
        while(set.contains(tmpValue)) {
            tmpValue = value + index;
            index++;
        }
        return tmpValue;
    }

    private void setVarcharForNull() {
        Iterator<String> iterator = headerTypeMap.keySet().iterator();
        while(iterator.hasNext()) {
            String entry = iterator.next();
            if(headerTypeMap.get(entry).equals(CSVUtils.NULL)) {
                headerTypeMap.put(entry, CSVUtils.VARCHAR);
            }
        }
    }

    private void changeTypeFromDate(String header, String value, int index) {
        if(CSVUtils.isDateTime(value)) {
            prepareAllHeadermap(header, CSVUtils.DATE_TIME, index);
        } else {
            prepareAllHeadermap(header, CSVUtils.VARCHAR, index);
        }
    }

    private void changeTypeFromTime(String header, String value, int index) {
        if(CSVUtils.isDateTime(value)) {
            prepareAllHeadermap(header, CSVUtils.DATE_TIME, index);
        } else {
            prepareAllHeadermap(header, CSVUtils.VARCHAR, index);
        }
    }

    private void changeTypeFromInteger(String header, String value, int index) {
        if(CSVUtils.isDoubleType(value)) {       // row[index]
            prepareAllHeadermap(header, CSVUtils.FLOAT, index);  //headers[index]
            headerReverseIndexTypeMap.put(index, CSVUtils.FLOAT);
        } else if(CSVUtils.isPercentage(value)) {        //row[index]
            prepareAllHeadermap(header, CSVUtils.PERCENTAGE, index);     //headers[index]
            headerReverseIndexTypeMap.put(index, CSVUtils.PERCENTAGE);
        } else {
            prepareAllHeadermap(header, CSVUtils.VARCHAR, index);        //headers[index]
            headerReverseIndexTypeMap.put(index, CSVUtils.VARCHAR);
        }
    }


    /**
     * Return List of Map with contains the data.
     * Values are assigned by changing its type to which
     * is identified in checkDataType.
     * For percentage data type percentage sign is removed
     * and FLOAT data type is assigned
     *
     * @return List<Map>
     */
    private List<Map<String, Object>> prepareValueMapList() {

        List<Map<String, Object>> mapList = new ArrayList<>();
        String[] headerValues = prepareStringArrayOfHeader(list.get(0));

        for(int i=1; i<list.size(); i++) {
            Map<String, Object> valueMap = new HashMap<>();
            String[] values = list.get(i);
            for(int j = 0; j<values.length; j++) {

                switch (headerReverseIndexTypeMap.get(j)) {
                    case CSVUtils.INTEGER:
                        valueMap.put(headerValues[j], assignIntegerValToMap(values[j]));
                        break;
                    case CSVUtils.FLOAT:
                        valueMap.put(headerValues[j], assignFloatValToMap(values[j]));
                        break;
                    case CSVUtils.PERCENTAGE:
                        valueMap.put(headerValues[j], assignPercentageValToMap(values[j]));
                        break;
                    default:
                        valueMap.put(headerValues[j], assignStringValToMap(values[j]));
                        break;
                }
            }
            mapList.add(valueMap);
        }

        return mapList;
    }

    private String assignStringValToMap(String value) {
        if(StringUtils.isNotBlank(value)) {
            return value;
        }
        return null;
    }

    private Float assignPercentageValToMap(String value) {
        if(StringUtils.isNotBlank(value)) {
            return Float.parseFloat(value.trim().replace("%", ""));
        }
        return null;
    }

    private Float assignFloatValToMap(String value) {
        if(StringUtils.isNotBlank(value)) {
            return Float.parseFloat(value.trim());
        }
        return null;
    }

    private Integer assignIntegerValToMap(String value) {
        if(StringUtils.isNotBlank(value)) {
            return Integer.parseInt(value.trim());
        }
        return null;
    }

    private void prepareAllHeadermap(String fieldName, String dataType, int index) {
        headerTypeMap.put(fieldName, dataType);
        headerNonStringypeMap.put(fieldName, dataType);
        headerReverseIndexTypeMap.put(Integer.valueOf(index), dataType);
    }

    private boolean isIntegerWithNonIntegerValue(String value, String header) {
        return headerTypeMap.get(header).equals(CSVUtils.INTEGER) && !CSVUtils.isIntegerType(value);
    }

    private boolean isFloatWithNonFloatValue(String value, String header) {
        return headerTypeMap.get(header).equals(CSVUtils.FLOAT) && !CSVUtils.isDoubleType(value);
    }

    private boolean isTimeWithNonTimeValue(String value, String header) {
        return headerTypeMap.get(header).equals(CSVUtils.TIME) && !CSVUtils.isTime(value);
    }

    private boolean isDateWithNonDateValue(String value, String header) {
        return headerTypeMap.get(header).equals(CSVUtils.DATE) && !CSVUtils.isDate(value);
    }

    private boolean isDatetimeWithNonDatetimeValue(String value, String header) {
        return headerTypeMap.get(header).equals(CSVUtils.DATE_TIME) && !CSVUtils.isDateTime(value);
    }

    private boolean isPercentageWithNonPercentageValue(String value, String header) {
        return headerTypeMap.get(header).equals(CSVUtils.PERCENTAGE) && !CSVUtils.isPercentage(value);
    }
}
