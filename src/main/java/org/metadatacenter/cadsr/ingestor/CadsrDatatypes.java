package org.metadatacenter.cadsr.ingestor;

import com.google.common.collect.Lists;

import java.util.List;

public class CadsrDatatypes {

  public static final String CHARACTER = "CHARACTER";
  public static final String NUMBER = "NUMBER";
  public static final String JAVA_STRING = "java.lang.String";
  public static final String DATE = "DATE";
  public static final String JAVA_LONG = "java.lang.Long";
  public static final String JAVA_INTEGER = "java.lang.Integer";
  public static final String JAVA_DATE = "java.util.Date";
  public static final String ALPHANUMERIC = "ALPHANUMERIC";
  public static final String ISO21090CD = "ISO21090CDv1.0";
  public static final String JAVA_DOUBLE = "java.lang.Double";

  public static final List<String> STRING_LIST = Lists.newArrayList(
      CHARACTER, JAVA_STRING, ALPHANUMERIC, ISO21090CD
  );

  public static final List<String> NUMERIC_LIST = Lists.newArrayList(
      NUMBER
  );

  public static final List<String> DATE_LIST = Lists.newArrayList(
      DATE, JAVA_DATE
  );

  public static final List<String> ALL_DATATYPES = Lists.newArrayList();
  static {
    ALL_DATATYPES.addAll(STRING_LIST);
    ALL_DATATYPES.addAll(NUMERIC_LIST);
    ALL_DATATYPES.addAll(DATE_LIST);
  }
}
