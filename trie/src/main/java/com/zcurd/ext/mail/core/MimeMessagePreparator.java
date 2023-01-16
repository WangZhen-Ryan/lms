package com.zcurd.ext.mail.core;

import javax.mail.internet.MimeMessage;

public interface MimeMessagePreparator {
  void prepare(MimeMessage paramMimeMessage) throws Exception;
}
