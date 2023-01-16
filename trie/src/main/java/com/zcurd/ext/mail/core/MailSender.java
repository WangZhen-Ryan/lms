package com.zcurd.ext.mail.core;

import com.zcurd.ext.mail.exception.MailException;

public interface MailSender {
  void send(SimpleMailMessage paramSimpleMailMessage) throws MailException;
  
  void send(SimpleMailMessage[] paramArrayOfSimpleMailMessage) throws MailException;
}
