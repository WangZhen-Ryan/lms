package com.zcurd.ext.mail.exception;

public class MailPreparationException extends MailException {
  private static final long serialVersionUID = -2430875949743255277L;
  
  public MailPreparationException(String msg) { super(msg); }
  
  public MailPreparationException(String msg, Throwable cause) { super(msg, cause); }
  
  public MailPreparationException(Throwable cause) { super("Could not prepare mail", cause); }
}
