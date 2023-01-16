package com.zcurd.ext.mail.exception;

public abstract class MailException extends RuntimeException {
  private static final long serialVersionUID = 2074009899265906631L;
  
  public MailException(String msg) { super(msg); }
  
  public MailException(String msg, Throwable cause) { super(msg, cause); }
}
