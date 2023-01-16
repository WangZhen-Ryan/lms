package com.zcurd.ext.mail.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MailSendException extends MailException {
  private static final long serialVersionUID = -8052224946187662173L;
  
  private final Map<Object, Exception> failedMessages;
  
  private Exception[] messageExceptions;
  
  public MailSendException(String msg) { this(msg, null); }
  
  public MailSendException(String msg, Throwable cause) {
    super(msg, cause);
    this.failedMessages = new LinkedHashMap();
  }
  
  public MailSendException(String msg, Throwable cause, Map<Object, Exception> failedMessages) {
    super(msg, cause);
    this.failedMessages = new LinkedHashMap(failedMessages);
    this.messageExceptions = (Exception[])failedMessages.values().toArray(new Exception[failedMessages.size()]);
  }
  
  public MailSendException(Map<Object, Exception> failedMessages) { this(null, null, failedMessages); }
  
  public final Map<Object, Exception> getFailedMessages() { return this.failedMessages; }
  
  public final Exception[] getMessageExceptions() { return (this.messageExceptions != null) ? this.messageExceptions : new Exception[0]; }
  
  public String getMessage() {
    if (isEmpty(this.messageExceptions))
      return super.getMessage(); 
    StringBuilder sb = new StringBuilder();
    String baseMessage = super.getMessage();
    if (baseMessage != null)
      sb.append(baseMessage).append(". "); 
    sb.append("Failed messages: ");
    for (int i = 0; i < this.messageExceptions.length; i++) {
      Exception subEx = this.messageExceptions[i];
      sb.append(subEx.toString());
      if (i < this.messageExceptions.length - 1)
        sb.append("; "); 
    } 
    return sb.toString();
  }
  
  public String toString() {
    if (isEmpty(this.messageExceptions))
      return super.toString(); 
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append("; message exceptions (").append(this.messageExceptions.length).append(") are:");
    for (int i = 0; i < this.messageExceptions.length; i++) {
      Exception subEx = this.messageExceptions[i];
      sb.append('\n').append("Failed message ").append(i + 1).append(": ");
      sb.append(subEx);
    } 
    return sb.toString();
  }
  
  public void printStackTrace(PrintStream ps) {
    if (isEmpty(this.messageExceptions)) {
      super.printStackTrace(ps);
    } else {
      ps.println(String.valueOf(super.toString()) + "; message exception details (" + this.messageExceptions.length + ") are:");
      for (int i = 0; i < this.messageExceptions.length; i++) {
        Exception subEx = this.messageExceptions[i];
        ps.println("Failed message " + (i + 1) + ":");
        subEx.printStackTrace(ps);
      } 
    } 
  }
  
  public void printStackTrace(PrintWriter pw) {
    if (isEmpty(this.messageExceptions)) {
      super.printStackTrace(pw);
    } else {
      pw.println(String.valueOf(super.toString()) + "; message exception details (" + this.messageExceptions.length + ") are:");
      for (int i = 0; i < this.messageExceptions.length; i++) {
        Exception subEx = this.messageExceptions[i];
        pw.println("Failed message " + (i + 1) + ":");
        subEx.printStackTrace(pw);
      } 
    } 
  }
  
  public static boolean isEmpty(Object[] array) { return !(array != null && array.length != 0); }
}
