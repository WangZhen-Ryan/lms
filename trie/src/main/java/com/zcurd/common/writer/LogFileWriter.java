package com.zcurd.common.writer;

import com.jfinal.log.Log;
import java.io.IOException;
import java.io.Writer;

public class LogFileWriter extends Writer {
  private Log logger = Log.getLog(getClass());
  
  public void write(String str) throws IOException { this.logger.info(str); }
  
  public void write(char[] cbuf, int off, int len) throws IOException {}
  
  public void flush() {}
  
  public void close() {}
}
