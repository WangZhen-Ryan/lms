package com.zcurd.common.handler;

import com.zcurd.common.util.PasswordUtil;
import com.zcurd.vo.ZcurdMeta;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class PasswordCurdHandle implements CurdHandle {
  public void add(ZcurdMeta zcurdMeta, HttpServletRequest req, Map<String, String[]> paraMap) {
    String password = "123456";
    String[] passwordPara = (String[])paraMap.get("model.password");
    if (passwordPara != null && passwordPara.length > 0)
      password = passwordPara[0]; 
    paraMap.put("model.password", new String[] { PasswordUtil.encodePassword(password) });
  }
  
  public void update(ZcurdMeta zcurdMeta, HttpServletRequest req, Map<String, String[]> paraMap) { System.out.println("------------------CurdHandle to update!"); }
  
  public void delete(ZcurdMeta zcurdMeta, HttpServletRequest req, Map<String, String[]> paraMap) { System.out.println("------------------CurdHandle to delete!"); }
}
