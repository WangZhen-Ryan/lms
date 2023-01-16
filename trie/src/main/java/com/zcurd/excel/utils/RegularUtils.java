package com.zcurd.excel.utils;

import com.zcurd.excel.exceptions.IllegalGroupIndexException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularUtils {
  public static boolean isMatched(String pattern, String reg) {
    Pattern compile = Pattern.compile(reg);
    return compile.matcher(pattern).matches();
  }
  
  public static List<String> match(String pattern, String reg, int group) throws IllegalGroupIndexException {
    List<String> matchGroups = new ArrayList<String>();
    Pattern compile = Pattern.compile(reg);
    Matcher matcher = compile.matcher(pattern);
    if (group > matcher.groupCount() || group < 0)
      throw new IllegalGroupIndexException("Illegal match group :" + group); 
    while (matcher.find())
      matchGroups.add(matcher.group(group)); 
    return matchGroups;
  }
  
  public static String match(String pattern, String reg) {
    String match = null;
    try {
      List<String> matchs = match(pattern, reg, 0);
      if (matchs != null && matchs.size() > 0)
        match = (String)matchs.get(0); 
    } catch (IllegalGroupIndexException e) {
      e.printStackTrace();
    } 
    return match;
  }
  
  public static String converNumByReg(String number) {
    Pattern compile = Pattern.compile("^(\\d+)(\\.0*)?$");
    Matcher matcher = compile.matcher(number);
    while (matcher.find())
      number = matcher.group(1); 
    return number;
  }
}
