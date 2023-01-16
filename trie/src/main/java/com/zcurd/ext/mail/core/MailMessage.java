package com.zcurd.ext.mail.core;

import com.zcurd.ext.mail.exception.MailParseException;
import java.util.Date;

public interface MailMessage {
  void setFrom(String paramString) throws MailParseException;
  
  void setReplyTo(String paramString) throws MailParseException;
  
  void setTo(String paramString) throws MailParseException;
  
  void setTo(String[] paramArrayOfString) throws MailParseException;
  
  void setCc(String paramString) throws MailParseException;
  
  void setCc(String[] paramArrayOfString) throws MailParseException;
  
  void setBcc(String paramString) throws MailParseException;
  
  void setBcc(String[] paramArrayOfString) throws MailParseException;
  
  void setSentDate(Date paramDate) throws MailParseException;
  
  void setSubject(String paramString) throws MailParseException;
  
  void setText(String paramString) throws MailParseException;
}
