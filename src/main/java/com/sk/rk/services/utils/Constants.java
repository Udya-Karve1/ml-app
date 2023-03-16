package com.sk.rk.services.utils;

/**
 *
 * @author uday.karve
 * @author pinkal.solanki
 */
public class Constants {

	private Constants() {
	}

	public static final String HEADER_USER_NAME = "user-name";
	public static final String DEFAULT_INVOKING_USER = "no-user";
	public static final char FILE_PATH_SEPERATOR = '/';

	public static final String USER = "user";
	public static final String TEMP = "temp";
	public static final String CSV = "csv";
	public static final String COMA = ",";
	public static final String FILE_TYPE_CSV = "csv";


	public static final String SWAGGER_NOTE = "Possible custom formula:\r\n"
			+ "values seperated by commas,\r\n"
			+ "\r\n"
			+ " eg. SUM(A,B,C,D,E...)\r\n"
			+ "	 SUB(A,B,C,D...)\r\n"
			+ "	 MUL(A,B,C,D,E...)\r\n"
			+ "	 DIV(A,B)\r\n"
			+ "	 POW(A,B)\r\n"
			+ "	 AVG(A,B,C,D,E...)\r\n"
			+ "	 PERCENTAGE(obtained,total)\r\n"
			+ "	 DATEDIFF's return type possible values {MINUTE, HOUR, DAY, WEEK, MONTH, YEAR}\r\n"
			+ "	 DATEDIFF(DAY, yyyy-MM-dd hh:mm, yyyy-MM-dd hh:mm) :: function returns date difference in number of days\r\n"
			+ "	 DATEDIFF(DAY, 2021-07-01 00:00, 2021-07-22 00:00) :: function returns date difference in number of days\r\n"
			+ "	 DATEDIFF(WEEK, 2021-07-01 00:00, 2021-07-22 00:00) :: function returns date difference in number of weeks";


	public static final String FILE_NAME_ESC_CHARS = "[\n|\r|\t]";










	public static final String HEADER_USER_ID                   = "user-id";
	public static final String START_TIME                       = "startTime";
	public static final String ONLY_DATE_FORMAT                 = "MM/dd/yyyy";
	public static final String YYYY_MM_DD_HH_MM_SS              = "yyyy_MM_dd_hh_mm_ss";
	public static final String LONG_DATE_FORMAT                 = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String DURATION_FIELD                   = "duration";
	public static final String DATA_FIELD                       = "data";

	public static final String RESPONSE_USER_MESSAGE_FIELD      = "userMessage";
	public static final String RESPONSE_CODE                    = "code";
	public static final String RESPONSE_SYSTEM_MESSAGE_FIELD    = "systemMessage";
	public static final String RESPONSE_PATH_FIELD              = "path";
	public static final String RESPONSE_EXCEPTION_FIELD         = "exception";

	public static final String TIMESTAMP_FIELD                  = "timestamp";
	public static final String IS_SUCCESS                       = "isSuccess";
	public static final String OUTPUT                           = "MessageText";
	public static final String INVOKING_USER                    = "InvokingUser";
	public static final String SUCCESS                          = "Success";

	public static final String BYTE = "Byte";
	public static final String SHORT = "Short";
	public static final String BIG_INTEGER = "BigInteger";
	public static final String INTEGER = "Integer";
	public static final String LONG = "Long";

	public static final String USER_SESSION = "user-session";

	public static final String[] CLASSIFICATION_TYPES = new String[]{"Logistic Regression","Decision Tree","Random forest","Support vector machine","K-nearest neighbour","Naive bayes"};
	public static final String[] REGRESSION_TYPES = {"Random Forest", "KNN Model","Support Vector Machines","Gausian Regression","Polynomial Regression"};




}
