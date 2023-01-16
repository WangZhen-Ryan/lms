package com.zcurd.common.util;

import com.jfinal.log.Log;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ToolDateTime {
  private static final Log log = Log.getLog(ToolDateTime.class);
  
  public static final String pattern_ym = "yyyy-MM";
  
  public static final int pattern_ym_length = 7;
  
  public static final String pattern_ymd = "yyyy-MM-dd";
  
  public static final int pattern_ymd_length = 10;
  
  public static final String pattern_ymd_hm = "yyyy-MM-dd HH:mm";
  
  public static final int pattern_ymd_hm_length = 16;
  
  public static final String pattern_ymd_hms = "yyyy-MM-dd HH:mm:ss";
  
  public static final int pattern_ymd_hms_length = 19;
  
  public static final String pattern_ymd_hms_s = "yyyy-MM-dd HH:mm:ss:SSS";
  
  public static final int pattern_ymd_hms_s_length = 23;
  
  public static Timestamp getSqlTimestamp() { return getSqlTimestamp((new Date()).getTime()); }
  
  public static Timestamp getSqlTimestamp(Date date) {
    if (date == null)
      return getSqlTimestamp(); 
    return getSqlTimestamp(date.getTime());
  }
  
  public static Timestamp getSqlTimestamp(long time) { return new Timestamp(time); }
  
  public static Timestamp getSqlTimestamp(String date, String pattern) {
    SimpleDateFormat format = new SimpleDateFormat(pattern);
    try {
      return new Timestamp(format.parse(date).getTime());
    } catch (ParseException e) {
      if (log.isErrorEnabled())
        log.error("ToolDateTime.parse异常：date值" + date + "，pattern值" + pattern, e); 
      return null;
    } 
  }
  
  public static Date getDate() { return new Date(); }
  
  public static Date getDate(int date, int hour, int minute, int second, int millisecond) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(5, date);
    calendar.set(11, hour);
    calendar.set(12, minute);
    calendar.set(13, second);
    calendar.set(14, millisecond);
    return calendar.getTime();
  }
  
  public static long getDateByTime() { return (new Date()).getTime(); }
  
  public static String format(Date date, String pattern) {
    DateFormat format = new SimpleDateFormat(pattern);
    return format.format(date);
  }
  
  public static String format(Date date, String pattern, TimeZone timeZone) {
    DateFormat format = new SimpleDateFormat(pattern);
    format.setTimeZone(timeZone);
    return format.format(date);
  }
  
  public static String format(String date, String parsePattern, String returnPattern) { return format(parse(date, parsePattern), returnPattern); }
  
  public static String format(String date, String parsePattern, String returnPattern, TimeZone timeZone) { return format(parse(date, parsePattern), returnPattern, timeZone); }
  
  public static Date parse(String date, String pattern) {
    SimpleDateFormat format = new SimpleDateFormat(pattern);
    try {
      return format.parse(date);
    } catch (ParseException e) {
      if (log.isErrorEnabled())
        log.error("ToolDateTime.parse异常：date值" + date + "，pattern值" + pattern, e); 
      return null;
    } 
  }
  
  public static Date parse(String dateStr) {
    Date date = null;
    try {
      date = DateFormat.getDateTimeInstance().parse(dateStr);
    } catch (ParseException e) {
      if (log.isErrorEnabled())
        log.error("ToolDateTime.parse异常：date值" + date, e); 
      return null;
    } 
    return date;
  }
  
  public static String getBetween(Date start, Date end) {
    long between = (end.getTime() - start.getTime()) / 1000L;
    long day = between / 86400L;
    long hour = between % 86400L / 3600L;
    long minute = between % 3600L / 60L;
    long second = between % 60L / 60L;
    StringBuilder sb = new StringBuilder();
    sb.append(day);
    sb.append("天");
    sb.append(hour);
    sb.append("小时");
    sb.append(minute);
    sb.append("分");
    sb.append(second);
    sb.append("秒");
    return sb.toString();
  }
  
  public static int getDateMinuteSpace(Date start, Date end) { return (int)((end.getTime() - start.getTime()) / 60000L); }
  
  public static int getDateHourSpace(Date start, Date end) { return (int)((end.getTime() - start.getTime()) / 3600000L); }
  
  public static int getDateDaySpace(Date start, Date end) { return (int)((end.getTime() - start.getTime()) / 86400000L); }
  
  public static String getDateInWeek(Date date) {
    String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    int dayIndex = calendar.get(7) - 1;
    if (dayIndex < 0)
      dayIndex = 0; 
    return weekDays[dayIndex];
  }
  
  public static Date getDateReduceHour(Date date, long hourCount) {
    long time = date.getTime() - 3600000L * hourCount;
    Date dateTemp = new Date();
    dateTemp.setTime(time);
    return dateTemp;
  }
  
  public static List<Date> getDateSplit(Date start, Date end, long splitCount) {
    long startTime = start.getTime();
    long endTime = end.getTime();
    long between = endTime - startTime;
    long count = splitCount - 1L;
    long section = between / count;
    List<Date> list = new ArrayList<Date>();
    list.add(start);
    for (long i = 1L; i < count; i++) {
      long time = startTime + section * i;
      Date date = new Date();
      date.setTime(time);
      list.add(date);
    } 
    list.add(end);
    return list;
  }
  
  public static List<String> getDaySpaceDate(Date start, Date end) {
    Calendar fromCalendar = Calendar.getInstance();
    fromCalendar.setTime(start);
    fromCalendar.set(11, 0);
    fromCalendar.set(12, 0);
    fromCalendar.set(13, 0);
    fromCalendar.set(14, 0);
    Calendar toCalendar = Calendar.getInstance();
    toCalendar.setTime(end);
    toCalendar.set(11, 0);
    toCalendar.set(12, 0);
    toCalendar.set(13, 0);
    toCalendar.set(14, 0);
    List<String> dateList = new LinkedList<String>();
    long dayCount = (toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / 86400000L;
    if (dayCount < 0L)
      return dateList; 
    dateList.add(format(fromCalendar.getTime(), "yyyy-MM-dd"));
    for (int i = 0; i < dayCount; i++) {
      fromCalendar.add(5, 1);
      dateList.add(format(fromCalendar.getTime(), "yyyy-MM-dd"));
    } 
    return dateList;
  }
  
  public static Date startDateByDay(Date start, int end) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(start);
    calendar.add(5, end);
    calendar.set(11, 0);
    calendar.set(12, 0);
    calendar.set(13, 0);
    calendar.set(14, 0);
    return calendar.getTime();
  }
  
  public static Date endDateByDay(Date start) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(start);
    calendar.set(11, 23);
    calendar.set(12, 59);
    calendar.set(13, 59);
    calendar.set(14, 999);
    return calendar.getTime();
  }
  
  public static Date startDateByHour(Date start, int end) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(start);
    calendar.set(12, end);
    return calendar.getTime();
  }
  
  public static Date endDateByHour(Date end) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(end);
    calendar.set(13, 59);
    calendar.set(14, 999);
    return calendar.getTime();
  }
  
  public static Map<String, Date> getStartEndDateByWeek(int year, int week) {
    Calendar weekCalendar = new GregorianCalendar();
    weekCalendar.set(1, year);
    weekCalendar.set(3, week);
    weekCalendar.set(7, weekCalendar.getFirstDayOfWeek());
    Date startDate = weekCalendar.getTime();
    weekCalendar.roll(7, 6);
    Date endDate = weekCalendar.getTime();
    Calendar startCalendar = Calendar.getInstance();
    startCalendar.setTime(startDate);
    startCalendar.add(5, 1);
    startCalendar.set(11, 0);
    startCalendar.set(12, 0);
    startCalendar.set(13, 0);
    startCalendar.set(14, 0);
    startDate = startCalendar.getTime();
    Calendar endCalendar = Calendar.getInstance();
    endCalendar.setTime(endDate);
    endCalendar.add(5, 1);
    endCalendar.set(11, 23);
    endCalendar.set(12, 59);
    endCalendar.set(13, 59);
    endCalendar.set(14, 999);
    endDate = endCalendar.getTime();
    Map<String, Date> map = new HashMap<String, Date>();
    map.put("start", startDate);
    map.put("end", endDate);
    return map;
  }
  
  public static Map<String, Date> getMonthDate(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(11, 0);
    calendar.set(12, 0);
    calendar.set(13, 0);
    calendar.set(14, 0);
    calendar.set(5, calendar.getActualMinimum(5));
    Date start = calendar.getTime();
    calendar.set(5, calendar.getActualMaximum(5));
    Date end = calendar.getTime();
    Map<String, Date> map = new HashMap<String, Date>();
    map.put("start", start);
    map.put("end", end);
    return map;
  }
  
  public static <T> List<List<T>> splitList(List<T> list, int pageSize) {
    int listSize = list.size();
    int page = (listSize + pageSize - 1) / pageSize;
    List<List<T>> listArray = new ArrayList<List<T>>();
    for (int i = 0; i < page; i++) {
      List<T> subList = new ArrayList<T>();
      for (int j = 0; j < listSize; j++) {
        int pageIndex = (j + 1 + pageSize - 1) / pageSize;
        if (pageIndex == i + 1)
          subList.add(list.get(j)); 
        if (j + 1 == (j + 1) * pageSize)
          break; 
      } 
      listArray.add(subList);
    } 
    return listArray;
  }
}
