package com.zcurd.excel.handler;

public class ExcelHeader extends Object implements Comparable<ExcelHeader> {
  private String title;
  
  private int order;
  
  private String filed;
  
  private Class filedClazz;
  
  public String getTitle() { return this.title; }
  
  public void setTitle(String title) { this.title = title; }
  
  public int getOrder() { return this.order; }
  
  public void setOrder(int order) { this.order = order; }
  
  public String getFiled() { return this.filed; }
  
  public void setFiled(String filed) { this.filed = filed; }
  
  public Class getFiledClazz() { return this.filedClazz; }
  
  public void setFiledClazz(Class filedClazz) { this.filedClazz = filedClazz; }
  
  public int compareTo(ExcelHeader o) { return this.order - o.order; }
  
  public ExcelHeader(String title, int order, String filed, Class filedClazz) {
    this.title = title;
    this.order = order;
    this.filed = filed;
    this.filedClazz = filedClazz;
  }
}
