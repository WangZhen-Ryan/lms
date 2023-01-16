package com.zcurd.ext.mail.exception;

public class MailParseException extends MailException {
  private static final long serialVersionUID = -8089227557387828745L;
  
  public MailParseException(String msg) { super(msg); }
  
  public MailParseException(String msg, Throwable cause) { super(msg, cause); }
  
  public MailParseException(Throwable cause) { super("Could not parse mail", cause); }
}
