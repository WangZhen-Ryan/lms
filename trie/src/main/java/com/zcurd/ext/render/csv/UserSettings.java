package com.zcurd.ext.render.csv;

public class UserSettings {
  public static final int ESCAPE_MODE_DOUBLED = 1;
  
  public static final int ESCAPE_MODE_BACKSLASH = 2;
  
  public char textQualifier = '"';
  
  public boolean useTextQualifier = true;
  
  public char delimiter = ',';
  
  public char recordDelimiter = Character.MIN_VALUE;
  
  public char comment = '#';
  
  public int escapeMode = 1;
  
  public boolean forceQualifier = false;
}
