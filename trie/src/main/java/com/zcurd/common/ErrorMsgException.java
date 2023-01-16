package com.zcurd.common;

public class

ErrorMsgException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public ErrorMsgException(String string) { super(string); }
  
  public ErrorMsgException(String string, Throwable throwable) { super(string, throwable); }
}
