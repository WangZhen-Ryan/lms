package com.zcurd.ext.mail;

import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import java.util.Properties;

public class MailPlugin implements IPlugin {
  static final String MAIN_CONFIG = "main";
  
  String configName;
  
  Properties props;
  
  public MailPlugin(Properties props) { this("main", props); }
  
  public MailPlugin(String configName, Properties props) {
    this.configName = "main";
    this.configName = configName;
    this.props = props;
  }
  
  public boolean start() {
    if (StrKit.isBlank(this.props.getProperty("host")))
      throw new RuntimeException("未设置邮箱服务器"); 
    if (StrKit.isBlank(this.props.getProperty("protocol"))) {
      this.props.setProperty("protocol", "smtp");
      LogKit.warn("未设置协议protocol，使用默认值:smtp");
    } 
    if (StrKit.isBlank(this.props.getProperty("port")))
      throw new RuntimeException("未设置端口port"); 
    if (StrKit.isBlank(this.props.getProperty("mail.smtp.ssl.enable"))) {
      this.props.setProperty("mail.smtp.ssl.enable", "false");
      LogKit.warn("未设置mail.smtp.ssl.enable，使用默认值:false");
    } 
    if (StrKit.isBlank(this.props.getProperty("mail.smtp.auth"))) {
      this.props.setProperty("mail.smtp.auth", "true");
      LogKit.warn("未设置mail.smtp.auth，使用默认值:true");
    } 
    if (StrKit.isBlank(this.props.getProperty("username")))
      throw new RuntimeException("未设置用户名"); 
    if (StrKit.isBlank(this.props.getProperty("password")))
      LogKit.warn("未设置密码"); 
    if (StrKit.isBlank(this.props.getProperty("mail.smtp.timeout"))) {
      this.props.setProperty("mail.smtp.timeout", "10000");
      LogKit.warn("未设置超时时间，使用默认值mail.smtp.timeout:10000");
    } 
    MailKit.init(this.configName, new MailPro(this.props));
    return true;
  }
  
  public boolean stop() { return true; }
}
