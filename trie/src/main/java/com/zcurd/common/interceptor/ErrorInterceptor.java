package com.zcurd.common.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.zcurd.common.ErrorMsgException;
import java.util.HashMap;
import java.util.Map;

public class ErrorInterceptor implements Interceptor {
  private Log logger = Log.getLog(ErrorInterceptor.class);
  
  public void intercept(Invocation inv) {
    Controller c = inv.getController();
    try {
      inv.invoke();
    } catch (ErrorMsgException e) {
      this.logger.error("发生异常或有警告信息", e);
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("result", "fail");
      result.put("msg", e.getMessage());
      c.renderJson(result);
    } catch (Exception e) {
      this.logger.error("发生异常", e);
      String header = c.getRequest().getHeader("X-Requested-With");
      boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header);
      if (isAjax) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", "fail");
        result.put("msg", "对不起，操作失败。原因[" + e.getMessage() + "]");
        c.renderJson(result);
      } 
    } 
  }
}
