package com.zcurd.ext.mail;

import com.zcurd.ext.mail.core.JavaMailSender;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailKit {
  static Map<String, MailPro> proMap = new HashMap();
  
  static MailPro mailPro = null;
  
  static void init(String configName, MailPro mailPro) {
    if (proMap.get(configName) != null)
      throw new RuntimeException(String.valueOf(configName) + "配置的Mail已经存在！"); 
    proMap.put(configName, mailPro);
    if ("main".equals(configName))
      MailKit.mailPro = mailPro; 
  }
  
  public static MailPro use(String configName) {
    MailPro mailPro = (MailPro)proMap.get(configName);
    if (mailPro == null)
      throw new RuntimeException(String.valueOf(configName) + "配置的Mail不存在！"); 
    return mailPro;
  }
  
  public static void send(String to, List<String> cc, String subject, String text) { mailPro.send(to, cc, subject, text); }
  
  public static void send(String to, List<String> cc, String subject, String text, List<File> attachments) { mailPro.send(to, cc, subject, text, attachments); }
  
  public static void send(String to, List<String> cc, String subject, String viewpath, Map<String, Object> dataMap) { mailPro.send(to, cc, subject, viewpath, dataMap); }
  
  public static void send(String to, List<String> cc, String subject, String viewpath, Map<String, Object> dataMap, List<File> attachments) { mailPro.send(to, cc, subject, viewpath, dataMap, attachments); }
  
  public static JavaMailSender getMailSender() { return mailPro.getMailSender(); }
}
