package com.zcurd.excel.utils;

import com.jfinal.log.Log;
import com.zcurd.excel.exceptions.TimeMatchFormatException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtils {
  public static final String DATE_FORMAT_DAY = "yyyy-MM-dd";
  
  public static final String DATE_FORMAT_DAY_2 = "yyyy/MM/dd";
  
  public static final String TIME_FORMAT_SEC = "HH:mm:ss";
  
  public static final String DATE_FORMAT_SEC = "yyyy-MM-dd HH:mm:ss";
  
  public static final String DATE_FORMAT_MSEC = "yyyy-MM-dd HH:mm:ss.SSS";
  
  public static final String DATE_FORMAT_MSEC_T = "yyyy-MM-dd'T'HH:mm:ss.SSS";
  
  public static final String DATE_FORMAT_MSEC_T_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  
  private static final String DATE_REG = "^[1-9]\\d{3}-\\d{1,22}-\\d{1,2}$";
  
  private static final String DATE_REG_2 = "^[1-9]\\d{3}/\\d{1,2}/\\d{1,2}$";
  
  private static final String TIME_SEC_REG = "^(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d$";
  
  private static final String DATE_TIME_REG = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d$";
  
  private static final String DATE_TIME_MSEC_REG = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\\.\\d{3}$";
  
  private static final String DATE_TIME_MSEC_T_REG = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\\.\\d{3}$";
  
  private static final String DATE_TIME_MSEC_T_Z_REG = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\\.\\d{3}Z$";
  
  public static String date2Str(Date date, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(date);
  }
  
  public static String date2Str(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(date);
  }
  
  public static Date str2Date(String strDate, String format) {
    Date date = null;
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    try {
      date = sdf.parse(strDate);
    } catch (ParseException e) {
      e.printStackTrace();
    } 
    return date;
  }
  
  public static Date str2Date(String strDate) throws TimeMatchFormatException, ParseException {
    strDate = strDate.trim();
    SimpleDateFormat sdf = null;
    if (RegularUtils.isMatched(strDate, "^[1-9]\\d{3}-\\d{1,22}-\\d{1,2}$"))
      sdf = new SimpleDateFormat("yyyy-MM-dd"); 
    if (RegularUtils.isMatched(strDate, "^[1-9]\\d{3}/\\d{1,2}/\\d{1,2}$"))
      sdf = new SimpleDateFormat("yyyy/MM/dd"); 
    if (RegularUtils.isMatched(strDate, "^(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d$"))
      sdf = new SimpleDateFormat("HH:mm:ss"); 
    if (RegularUtils.isMatched(strDate, "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d$"))
      sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    if (RegularUtils.isMatched(strDate, "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\\.\\d{3}$"))
      sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
    if (RegularUtils.isMatched(strDate, "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\\.\\d{3}$"))
      sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); 
    if (RegularUtils.isMatched(strDate, "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\\.\\d{3}Z$"))
      sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); 
    if (sdf != null)
      return sdf.parse(strDate); 
    throw new TimeMatchFormatException(String.format("[%s] 未找到正确的格式化方式！", new Object[] { strDate }));
  }
  
  public static Date str2DateUnmatch2Null(String strDate) throws TimeMatchFormatException, ParseException {
    Date date = null;
    try {
      date = org.apache.commons.lang3.time.DateUtils.parseDate(strDate, new String[] { "yyyy/MM/dd", "yyyy-MM-dd", 
            "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "HH:mm:ss" });
    } catch (Exception e) {
      Log.getLog(DateUtils.class).warn("格式化时间发生异常", e);
      throw e;
    } 
    return date;
  }
  
  public static void main(String[] args) throws Exception {}
}
