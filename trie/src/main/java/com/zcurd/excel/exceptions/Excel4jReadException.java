package com.zcurd.excel.exceptions;

public class Excel4jReadException extends Exception {
  private static final long serialVersionUID = 6256478492145237839L;
  
  public Excel4jReadException() {}
  
  public Excel4jReadException(String message) { super(message); }
  
  public Excel4jReadException(String message, Throwable cause) { super(message, cause); }
}
