package com.zcurd.ext.mail.exception;

public class MailAuthenticationException extends MailException {
  private static final long serialVersionUID = 5675296894245634470L;
  
  public MailAuthenticationException(String msg) { super(msg); }
  
  public MailAuthenticationException(String msg, Throwable cause) { super(msg, cause); }
  
  public MailAuthenticationException(Throwable cause) { super("Authentication failed", cause); }
}
