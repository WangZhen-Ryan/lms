package com.zcurd.ldap;

import java.io.Serializable;

public class LdapServerinfoEntity implements Serializable {
  private static final long serialVersionUID = -6337087247035524220L;
  
  private String url;
  
  private String principal;
  
  private String credentials;
  
  private String basedn;
  
  private String filter;
  
  public String getUrl() { return this.url; }
  
  public void setUrl(String url) { this.url = url; }
  
  public String getPrincipal() { return this.principal; }
  
  public void setPrincipal(String principal) { this.principal = principal; }
  
  public String getCredentials() { return this.credentials; }
  
  public void setCredentials(String credentials) { this.credentials = credentials; }
  
  public String getBasedn() { return this.basedn; }
  
  public void setBasedn(String basedn) { this.basedn = basedn; }
  
  public String getFilter() { return this.filter; }
  
  public void setFilter(String filter) { this.filter = filter; }
}
