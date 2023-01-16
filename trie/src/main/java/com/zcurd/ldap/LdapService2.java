package com.zcurd.ldap;

import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.zcurd.model.SysDict;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LdapService2 {
  public String getDictVal(String dictKey) { return ((SysDict)SysDict.me
      .findFirst("select dict_value from sys_dict where dict_type='LDAP设置' and dict_key='" + dictKey + "'"))
      .getStr("dict_value"); }
  
  public LdapServerinfoEntity getLdapServerinfoEntity() {
    LdapServerinfoEntity entity = new LdapServerinfoEntity();
    entity.setUrl(getDictVal("URL"));
    entity.setCredentials(getDictVal("认证密码"));
    entity.setPrincipal(getDictVal("RootDn"));
    entity.setBasedn(getDictVal("UserDn"));
    entity.setFilter(PropKit.use("config.properties").get("ldap.search.filter"));
    return entity;
  }
  
  public NamingEnumeration<SearchResult> getAllUsersInfo(LdapServerinfoEntity ldapEntity) throws Exception {
    DirContext ctx = null;
    Properties env = new Properties();
    env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
    env.put("java.naming.security.authentication", "simple");
    env.put("com.sun.jndi.ldap.connect.pool", "true");
    env.put("java.naming.referral", "follow");
    String PROVIDER_URL = ldapEntity.getUrl();
    if (!PROVIDER_URL.endsWith("/"))
      PROVIDER_URL = String.valueOf(PROVIDER_URL) + "/"; 
    String SECURITY_PRINCIPAL = ldapEntity.getPrincipal();
    String SECURITY_CREDENTIALS = ldapEntity.getCredentials();
    String baseDNFetch = ldapEntity.getBasedn();
    String filterFetch = ldapEntity.getFilter();
    env.put("java.naming.provider.url", PROVIDER_URL);
    env.put("java.naming.security.principal", SECURITY_PRINCIPAL);
    env.put("java.naming.security.credentials", SECURITY_CREDENTIALS);
    String baseDN = baseDNFetch;
    String filter = "(" + filterFetch + ")";
    try {
      ctx = new InitialDirContext(env);
      LogKit.info("LDAP连接成功！");
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(2);
      return ctx.search(baseDN, filter, constraints);
    } catch (Exception e) {
      throw new Exception("get all users info error", e);
    } 
  }
  
  public Map<String, String> loginSearch(Map<String, String> map, LdapServerinfoEntity ldapEntity) {
    String PROVIDER_URL = ldapEntity.getUrl();
    if (!PROVIDER_URL.endsWith("/"))
      PROVIDER_URL = String.valueOf(PROVIDER_URL) + "/"; 
    String baseDNFetch = ldapEntity.getBasedn();
    ldapEntity.setPrincipal("CN=pingxial,OU=Users,OU=CNASB,OU=CN,DC=ad4,DC=ad,DC=alcatel,DC=com");
    DirContext ctx = null;
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
    env.put("java.naming.security.authentication", "simple");
    env.put("com.sun.jndi.ldap.connect.pool", "true");
    env.put("java.naming.provider.url", PROVIDER_URL);
    env.put("java.naming.security.principal", ldapEntity.getPrincipal());
    env.put("java.naming.security.credentials", (String)map.get("password"));
    String baseDN = baseDNFetch;
    String filter = "(&(CN=" + (String)map.get("user_name") + "))";
    LogKit.info("PROVIDER_URL:" + PROVIDER_URL);
    LogKit.info("principal:" + ldapEntity.getPrincipal());
    LogKit.info("principal:" + ldapEntity.getPrincipal().length());
    LogKit.info("password:" + (String)map.get("password"));
    LogKit.info("baseDN:" + baseDN);
    LogKit.info("filter:" + filter);
    try {
      LogKit.info("Start load ldap server:" + env);
      ctx = new InitialDirContext(env);
      LogKit.info("Load ldap server success");
      LogKit.info("---------------------------------------------------------------------");
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(2);
      NamingEnumeration<?> en = ctx.search(baseDN, filter, constraints);
      if (en == null) {
        LogKit.warn("域中不存在该用户！");
        map = null;
      } else {
        while (en.hasMoreElements()) {
          Object obj = en.nextElement();
          if (obj instanceof SearchResult) {
            SearchResult sr = (SearchResult)obj;
            if (sr.getAttributes().get("mail") != null)
              map.put("mail", sr.getAttributes().get("mail").get().toString()); 
            if (sr.getAttributes().get("displayName") != null)
              map.put("display_name", sr.getAttributes().get("displayName").get().toString()); 
          } 
        } 
      } 
    } catch (Exception e) {
      map = null;
      LogKit.error(e.getMessage(), e);
    } 
    return map;
  }
  
  public String getPrinciPal(String userName, LdapServerinfoEntity ldapEntity) {
    String PROVIDER_URL = ldapEntity.getUrl();
    if (!PROVIDER_URL.endsWith("/"))
      PROVIDER_URL = String.valueOf(PROVIDER_URL) + "/"; 
    String baseDNFetch = ldapEntity.getBasedn();
    DirContext ctx = null;
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
    env.put("java.naming.security.authentication", "simple");
    env.put("com.sun.jndi.ldap.connect.pool", "true");
    env.put("java.naming.provider.url", PROVIDER_URL);
    env.put("java.naming.security.principal", ldapEntity.getPrincipal());
    env.put("java.naming.security.credentials", ldapEntity.getCredentials());
    String baseDN = baseDNFetch;
    String princiPalPart = "";
    String filter = ldapEntity.getFilter().replaceAll("###", userName);
    try {
      System.out.println("Start load ldap server:" + env);
      ctx = new InitialDirContext(env);
      System.out.println("Load ldap server ");
      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(2);
      NamingEnumeration<?> en = ctx.search(baseDN, filter, constraints);
      if (en == null) {
        LogKit.warn("域中不存在该用户！");
      } else {
        int count = 0;
        while (en.hasMoreElements()) {
          Object obj = en.nextElement();
          if (obj instanceof SearchResult) {
            SearchResult sr = (SearchResult)obj;
            princiPalPart = sr.getAttributes().get("distinguishedName").get().toString();
          } 
          count++;
          if (count > 1)
            throw new Exception("统一用户获取登录用户结果不唯一，请联系管理员修改查询条件"); 
        } 
      } 
    } catch (Exception e) {
      LogKit.error("统一账号获取要认证登陆用户的PRINCIPAL发生异常", e);
    } 
    return princiPalPart;
  }
}
