package com.zcurd.ext.render.csv;

import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

public class CsvRender extends Render {
  private List<String> clomuns;
  
  private List<?> data;
  
  private String encodeType;
  
  private String fileName;
  
  private List<String> headers;
  
  public CsvRender(List<String> headers, List<?> data) {
    this.encodeType = "gbk";
    this.fileName = "default";
    this.headers = headers;
    this.data = data;
  }
  
  public static CsvRender me(List<String> headers, List<?> data) { return new CsvRender(headers, data); }
  
  public void render() {
    this.response.reset();
    PrintWriter out = null;
    String userAgent = this.request.getHeader("USER-AGENT");
    try {
      if (userAgent.contains("MSIE")) {
        this.fileName = URLEncoder.encode(this.fileName, "UTF8");
      } else if (userAgent.contains("Mozilla")) {
        this.fileName = new String(this.fileName.getBytes(), "ISO8859-1");
      } else {
        this.fileName = URLEncoder.encode(this.fileName, "UTF8");
      } 
      this.response.setContentType("application/vnd.ms-excel;charset=" + this.encodeType);
      this.response.setHeader("Content-Disposition", 
          "attachment;  filename=" + this.fileName + ".csv");
      out = this.response.getWriter();
      out.write(CsvUtil.createCSV(this.headers, this.data, this.clomuns));
    } catch (Exception e) {
      throw new RenderException(e);
    } finally {
      if (out != null) {
        out.flush();
        out.close();
      } 
    } 
  }
  
  public CsvRender clomuns(List<String> clomuns) {
    this.clomuns = clomuns;
    return this;
  }
  
  public CsvRender data(List<? extends Object> data) {
    this.data = data;
    return this;
  }
  
  public CsvRender encodeType(String encodeType) {
    this.encodeType = encodeType;
    return this;
  }
  
  public CsvRender fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }
  
  public CsvRender headers(List<String> headers) {
    this.headers = headers;
    return this;
  }
}
