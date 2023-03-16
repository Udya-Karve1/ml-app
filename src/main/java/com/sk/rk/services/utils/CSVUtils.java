package com.sk.rk.services.utils;

import java.util.regex.Pattern;

public class CSVUtils {

    public static final String DATE = "DATE";
    public static final String DATE_TIME = "DATETIME";
    public static final String TIME = "TIME";
    public static final String VARCHAR = "VARCHAR";
    public static final String INTEGER = "INTEGER";
    public static final String FLOAT = "FLOAT";
    public static final String PERCENTAGE = "PERCENTAGE";
    public static final String NULL = "NULL";

    public static final String DATA_TYPE = "DataType";
    public static final String COLUMN_NAME = "ColumnName";

    public static final String DOUBLE_REG_EX_PATTERN_STRING = "[+-]?[0-9]+(\\.[0-9]+)?([Ee][+-]?[0-9]+)?";
    public static final String INTEGER_REG_EX_PATTERN_STRING  = "[+-]?[0-9]+";
    public static final String BOOLEAN_REG_EX_PATTERN_STRING = "[+-]?[0-9]+";
    public static final String TIME_PATTERN_HOUR_MINUTE_STRING = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
    public static final String TIME_PATTERN_HOUR_MINUTE_SECOND_STRING = "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";
    public static final String DATE_PATTERN_STRING = "^(?:(?:(?:0?[13578]|1[02])(\\/|-|\\.)31)\\1|" +
            "(?:(?:0?[1,3-9]|1[0-2])(\\/|-|\\.)(?:29|30)\\2))" +
            "(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:0?2(\\/|-|\\.)29\\3" +
            "(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|" +
            "[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|" +
            "^(?:(?:0?[1-9])|(?:1[0-2]))(\\/|-|\\.)(?:0?[1-9]|1\\d|" +
            "2[0-8])\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})";

    public static final String DATE_TIME_HM_PATTERN_STRING = DATE_PATTERN_STRING + " ([01]?[0-9]|2[0-3]):[0-5][0-9]$" ;
    public static final String DATE_TIME_HMS_PATTERN_STRING = DATE_PATTERN_STRING + " ([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";
    public static final String PERCENTAGE_PATTERN_STRING = "(^100%|(\\.0{1,2})?$)|(^([1-9]([0-9])?|0)(\\.[0-9]{1,2})?%$)";
    public static final String PERCENTAGE_PATTERN_SPACE_STRING = "(^100 %(\\.0{1,2})?$)|(^([1-9]([0-9])?|0)(\\.[0-9]{1,2})? %$)";

    public static final Pattern doublePattern = Pattern.compile(DOUBLE_REG_EX_PATTERN_STRING);
    public static final Pattern integerPattern = Pattern.compile(INTEGER_REG_EX_PATTERN_STRING);
    public static final Pattern datePattern = Pattern.compile(DATE_PATTERN_STRING);
    public static final Pattern percentagePattern = Pattern.compile(PERCENTAGE_PATTERN_STRING);
    public static final Pattern percentageSpacePattern = Pattern.compile(PERCENTAGE_PATTERN_SPACE_STRING);
    public static final Pattern timeHMPattern = Pattern.compile(TIME_PATTERN_HOUR_MINUTE_STRING);
    public static final Pattern timeHMSPattern= Pattern.compile(TIME_PATTERN_HOUR_MINUTE_SECOND_STRING);

    private CSVUtils() {}

    public static boolean isIntegerType(String value) {
        return integerPattern.matcher(value).matches();
    }

    public static boolean isDoubleType(String value) {
        return doublePattern.matcher(value).matches();
    }

    public static boolean isTime(String value) {
        return isHourMinute(value) || isHourMinuteSecond(value);
    }

    public static boolean isHourMinute(String value) {
        return timeHMPattern.matcher(value).matches();
    }

    public static boolean isHourMinuteSecond(String value) {
        return timeHMSPattern.matcher(value).matches();
    }

    public static boolean isDate(String value) {
        return datePattern.matcher(value).matches();
    }

    public static boolean isDateTime(String value) {
        if(value.indexOf(' ')>1) {
            String[] values = value.split(" ");
            return values.length == 2 && isDate(values[0].trim()) && isTime(values[1].trim());
        }
        return false;
    }

    public static boolean isPercentage(String value) {
        return isPercentageWithoutSpace(value) || isPercentageWithSpace(value);
    }

    public static boolean isPercentageWithSpace(String value) {
        return percentageSpacePattern.matcher(value).matches();
    }

    public static boolean isPercentageWithoutSpace(String value) {
        return percentagePattern.matcher(value).matches();
    }

    public static String replaceSpecialChars(String value) {
        char[] charArr = value.toCharArray();
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<charArr.length; i++) {
            if(charArr[i] == ' ') {
                builder.append('_');
            } else if(Character.isLetterOrDigit(charArr[i])) {
                builder.append(charArr[i]);
            }
        }

        return builder.toString();
    }

}
