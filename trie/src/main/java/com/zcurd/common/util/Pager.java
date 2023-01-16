package com.zcurd.common.util;

import com.jfinal.plugin.activerecord.Record;
import java.util.List;

public class Pager {
  private int page;
  
  private int rows;
  
  private List<Record> dataList;
  
  public int getStartRow() { return (getPage() - 1) * this.rows; }
  
  public int getPage() {
    if (this.page == 0)
      this.page = 1; 
    return this.page;
  }
  
  public void setPage(int page) { this.page = page; }
  
  public int getRows() {
    if (this.rows == 0)
      this.rows = 20; 
    return this.rows;
  }
  
  public void setRows(int rows) { this.rows = rows; }
  
  public List<Record> getDataList() { return this.dataList; }
  
  public void setDataList(List<Record> dataList) { this.dataList = dataList; }
}
