package com.zcurd.common.interceptor;

import com.jfinal.aop.Duang;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.zcurd.model.SysUser;
import com.zcurd.service.LoginService;
import java.util.List;

public class MenuAuthInterceptor implements Interceptor {
  public void intercept(Invocation inv) {
    inv.invoke();
    Controller controller = inv.getController();
    Boolean authLoad = getAuthLoad(controller.getSessionAttr("authLoad"));
    SysUser sysUser = (SysUser)controller.getSessionAttr("sysUser");
    if (authLoad.booleanValue() && sysUser != null) {
      LoginService loginService = (LoginService)Duang.duang(LoginService.class);
      controller.setSessionAttr("menuList", loginService.getUserMenu(sysUser));
      List<String> noAuthUrl = loginService.getNoAuthUrl();
      controller.setSessionAttr("noAuthUrl", noAuthUrl);
      controller.setSessionAttr("noAuthBtnUrl", loginService.getNoAuthBtnUrl(sysUser));
      controller.setSessionAttr("noAuthDatarule", loginService.getNoAuthDatarule(sysUser));
      if ("lmssuperadmin@1".equals(sysUser.get("user_name"))) {
        controller.setSessionAttr("noAuthUrl", null);
        controller.setSessionAttr("noAuthBtnUrl", null);
        controller.setSessionAttr("pageBtnMap", null);
        controller.setSessionAttr("noAuthDatarule", null);
      } 
      controller.setSessionAttr("authLoad", Boolean.valueOf(false));
    } 
  }
  
  private Boolean getAuthLoad(Object sessionAttr) { return Boolean.valueOf((sessionAttr == null) ? true : Boolean.parseBoolean(sessionAttr.toString())); }
}
