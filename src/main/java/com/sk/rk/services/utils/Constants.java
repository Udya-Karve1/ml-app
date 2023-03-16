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

	public static final String CSV = "csv";
	public static final String FILE_TYPE_CSV = "csv";
	public static final String FILE_NAME_ESC_CHARS = "[\n|\r|\t]";

	public static final String RESPONSE_USER_MESSAGE_FIELD      = "userMessage";
	public static final String RESPONSE_CODE                    = "code";
	public static final String RESPONSE_PATH_FIELD              = "path";
	public static final String RESPONSE_EXCEPTION_FIELD         = "exception";
	public static final String TIMESTAMP_FIELD                  = "timestamp";
	public static final String IS_SUCCESS                       = "isSuccess";
	public static final String BYTE = "Byte";
	public static final String SHORT = "Short";
	public static final String BIG_INTEGER = "BigInteger";
	public static final String INTEGER = "Integer";
	public static final String LONG = "Long";
	public static final String USER_SESSION = "user-session";
	protected static final String[] CLASSIFICATION_TYPES = new String[]{"Logistic Regression","Decision Tree","Random forest","Support vector machine","K-nearest neighbour","Naive bayes"};
	protected static final String[] REGRESSION_TYPES = {"Random Forest", "KNN Model","Support Vector Machines","Gausian Regression","Polynomial Regression"};
}
