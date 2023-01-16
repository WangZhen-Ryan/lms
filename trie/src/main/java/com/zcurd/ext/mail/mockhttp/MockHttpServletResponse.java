package com.zcurd.ext.mail.mockhttp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MockHttpServletResponse implements InvocationHandler {
  private PrintWriter printWriter = new MockPrintWriter(new StringWriter());
  
  public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
    if ("getWriter".equals(method.getName()))
      return this.printWriter; 
    return null;
  }
}
