package com.zcurd.ext.mail.mockhttp;

import java.io.PrintWriter;
import java.io.Writer;

public class MockPrintWriter extends PrintWriter {
  private String content = null;
  
  public MockPrintWriter(Writer out) { super(out); }
  
  public void close() {
    this.content = this.out.toString();
    super.close();
  }
  
  public String toString() {
    try {
      close();
    } catch (Exception exception) {}
    return this.content;
  }
}
