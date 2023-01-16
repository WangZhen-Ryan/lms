package com.zcurd.ext.mail;

import com.jfinal.kit.LogKit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import org.apache.commons.mail.HtmlEmail;

public class SendMail {
  private String host;
  
  private String from;
  
  private String username;
  
  private String password;
  
  public SendMail(Map<String, String> map) {
    this.host = "";
    this.from = "";
    this.username = "";
    this.password = "";
    this.host = (String)map.get("host");
    this.from = (String)map.get("from");
    this.username = (String)map.get("username");
    this.password = (String)map.get("password");
  }
  
  public void sendMail(String subject, String mailConent, List<Map<String, String>> toList) {
    if (toList == null || toList.size() <= 0)
      return; 
    LogKit.warn("000000000000000000000000000000000000000000000000000000000000000000000");
    LogKit.warn("host:" + this.host);
    LogKit.warn("username:" + this.username);
    LogKit.warn("password:" + this.password);
    LogKit.warn("from:" + this.from);
    HtmlEmail email = new HtmlEmail();
    email.setHostName(this.host);
    email.setAuthentication(this.username, this.password);
    email.setCharset("utf-8");
    try {
      email.setFrom(this.from);
      List<InternetAddress> inter = new ArrayList<InternetAddress>();
      for (Map<String, String> toMap : toList) {
        inter.add(new InternetAddress((String)toMap.get("address"), (String)toMap.get("personal"), "utf-8"));
        LogKit.warn("address:" + (String)toMap.get("address"));
        LogKit.warn("personal:" + (String)toMap.get("personal"));
      } 
      email.setTo(inter);
      email.setSubject(subject);
      email.setHtmlMsg(mailConent);
      email.send();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}
