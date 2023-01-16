package com.zcurd.ext.mail;

import com.jfinal.render.RenderManager;
import com.zcurd.ext.mail.core.JavaMailSender;
import com.zcurd.ext.mail.core.JavaMailSenderImpl;
import com.zcurd.ext.mail.core.MimeMessageHelper;
import com.zcurd.ext.mail.exception.MailSendException;
import com.zcurd.ext.mail.mockhttp.MockHttpServletRequest;
import com.zcurd.ext.mail.mockhttp.MockHttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

public class MailPro {
  private JavaMailSenderImpl mailSender;
  
  private String from;
  
  public MailPro(Properties props) {
    this.mailSender = null;
    this.from = null;
    this.mailSender = new JavaMailSenderImpl();
    this.mailSender.setHost(props.getProperty("host"));
    this.mailSender.setPort(Integer.parseInt(props.getProperty("port")));
    this.mailSender.setUsername(props.getProperty("username"));
    this.mailSender.setPassword(props.getProperty("password"));
    this.mailSender.setProtocol(props.getProperty("protocol", "smtp"));
    this.mailSender.setJavaMailProperties(props);
    this.from = props.getProperty("username");
  }
  
  public MailPro(Map<String, String> map) {
    this.mailSender = null;
    this.from = null;
    Properties props = convertMapToProperties(map);
    this.mailSender = new JavaMailSenderImpl();
    this.mailSender.setHost(props.getProperty("host"));
    this.mailSender.setPort(Integer.parseInt(props.getProperty("port")));
    this.mailSender.setUsername(props.getProperty("username"));
    this.mailSender.setPassword(props.getProperty("password"));
    this.mailSender.setProtocol(props.getProperty("protocol", "smtp"));
    this.mailSender.setJavaMailProperties(props);
    this.from = props.getProperty("username");
  }
  
  public void send(String to, List<String> cc, String subject, String text) { send(to, cc, subject, text, new ArrayList()); }
  
  public void send(String to, List<String> cc, String subject, String text, List<File> attachments) {
    MimeMessage mimeMessage = this.mailSender.createMimeMessage();
    try {
      MimeMessageHelper mimeMessageHelper = null;
      if (attachments != null && attachments.size() > 0) {
        mimeMessageHelper = new MimeMessageHelper(mimeMessage, 1, "UTF-8");
      } else {
        mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
      } 
      String[] split = StringUtils.split(to, ",");
      mimeMessageHelper.setTo(split);
      if (cc != null)
        mimeMessageHelper.setCc((String[])cc.toArray(new String[0])); 
      mimeMessageHelper.setSubject(subject);
      mimeMessageHelper.setFrom(this.from);
      mimeMessageHelper.setText(text, true);
      if (attachments != null && attachments.size() > 0)
        for (File file : attachments)
          mimeMessageHelper.addAttachment(file.getName(), file);  
      this.mailSender.send(mimeMessage);
    } catch (Exception e) {
      throw new MailSendException("邮件发送失败！", e);
    } 
  }
  
  public void send(String to, List<String> cc, String subject, String viewPath, Map<String, Object> dataMap) { send(to, cc, subject, viewPath, dataMap, null); }
  
  public void send(String to, List<String> cc, String subject, String viewPath, Map<String, Object> dataMap, List<File> attachments) {
    HttpServletResponse response = (HttpServletResponse)Proxy.newProxyInstance(getClass().getClassLoader(), 
        new Class[] { HttpServletResponse.class }, new MockHttpServletResponse());
    HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(getClass().getClassLoader(), 
        new Class[] { HttpServletRequest.class }, new MockHttpServletRequest());
    for (Map.Entry<String, Object> entry : dataMap.entrySet())
      request.setAttribute((String)entry.getKey(), entry.getValue()); 
    RenderManager.me().getRenderFactory().getRender(viewPath).setContext(request, response).render();
    try {
      String text = response.getWriter().toString();
      send(to, cc, subject, text, attachments);
    } catch (IOException e) {
      throw new MailSendException("邮件发送失败！", e);
    } 
  }
  
  public Properties convertMapToProperties(Map<String, String> map) {
    Properties properties = new Properties();
    Set<Map.Entry<String, String>> entrySet = map.entrySet();
    for (Map.Entry<String, String> entry : entrySet)
      properties.put(entry.getKey(), entry.getValue()); 
    return properties;
  }
  
  public JavaMailSender getMailSender() { return this.mailSender; }
}
