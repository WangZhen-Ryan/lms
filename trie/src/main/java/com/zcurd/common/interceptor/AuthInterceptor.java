package com.zcurd.common.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.zcurd.common.util.UrlUtil;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class AuthInterceptor implements Interceptor {
  public void intercept(Invocation inv) {
    Controller c = inv.getController();
    HttpServletRequest request = c.getRequest();
    int contextLength = request.getContextPath().length();
    String currUrl = request.getRequestURI().substring(contextLength);
    List<String> noAuthUrl = (List)c.getSessionAttr("noAuthUrl");
    if (noAuthUrl != null) {
      for (String url : noAuthUrl) {
        if (UrlUtil.formatBaseUrl(currUrl).equals(UrlUtil.formatBaseUrl(url))) {
          c.renderText("没有权限访问该页面！");
          return;
        } 
      } 
      Map<String, Object> authBtn = (Map)c.getSessionAttr("noAuthBtnUrl");
      List<String> noAuthBtnUrl = (List)authBtn.get("btnUrlList");
      Map<String, String> noAuthBtnMap = (Map)authBtn.get("pageBtnMap");
      request.setAttribute("noAuthBtn", noAuthBtnMap.get(currUrl));
      for (String btnUrl : noAuthBtnUrl) {
        if (currUrl.equals(btnUrl)) {
          c.renderText("没有权限访问该页面！");
          return;
        } 
      } 
    } 
    inv.invoke();
  }
}
