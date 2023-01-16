package com.zcurd.ext.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class EmailAuthenticator extends Authenticator {
  private String username;
  
  private String userpass;
  
  public String getUsername() { return this.username; }
  
  public void setUsername(String username) { this.username = username; }
  
  public String getUserpass() { return this.userpass; }
  
  public void setUserpass(String userpass) { this.userpass = userpass; }
  
  public EmailAuthenticator(String username, String userpass) {
    this.username = username;
    this.userpass = userpass;
  }
  
  public PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(this.username, this.userpass); }
}
