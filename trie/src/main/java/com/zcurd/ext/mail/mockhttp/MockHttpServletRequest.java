package com.zcurd.ext.mail.mockhttp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MockHttpServletRequest implements InvocationHandler {
  private Map dataMap = new HashMap();
  
  public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
    if ("getAttributeNames".equals(method.getName()))
      return Collections.enumeration(this.dataMap.keySet()); 
    if ("setAttribute".equals(method.getName()))
      return this.dataMap.put(objects[0], objects[1]); 
    if ("getAttribute".equals(method.getName()))
      return this.dataMap.get(objects[0]); 
    return null;
  }
}
