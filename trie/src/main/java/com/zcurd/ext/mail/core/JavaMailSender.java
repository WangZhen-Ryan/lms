package com.zcurd.ext.mail.core;

import com.zcurd.ext.mail.exception.MailException;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;

public interface JavaMailSender extends MailSender {
  MimeMessage createMimeMessage();
  
  MimeMessage createMimeMessage(InputStream paramInputStream) throws MailException;
  
  void send(MimeMessage paramMimeMessage) throws MailException;
  
  void send(MimeMessage[] paramArrayOfMimeMessage) throws MailException;
  
  void send(MimeMessagePreparator paramMimeMessagePreparator) throws MailException;
  
  void send(MimeMessagePreparator[] paramArrayOfMimeMessagePreparator) throws MailException;
}
