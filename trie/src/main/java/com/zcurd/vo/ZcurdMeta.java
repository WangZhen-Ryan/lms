package com.zcurd.vo;

import com.zcurd.model.ZcurdField;
import com.zcurd.model.ZcurdHead;
import com.zcurd.model.ZcurdHeadBtn;
import com.zcurd.model.ZcurdHeadJs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZcurdMeta {
  private ZcurdHead head;
  
  private List<ZcurdField> fieldList;
  
  private Map<String, Map<String, Object>> dictMap;
  
  private List<ZcurdField> addFieldList;
  
  private List<ZcurdField> updateFieldList;
  
  private List<ZcurdField> footerFieldList;
  
  private List<ZcurdHeadBtn> btnList;
  
  private List<ZcurdHeadBtn> topList;
  
  private List<ZcurdHeadBtn> lineList;
  
  private List<ZcurdHeadJs> jsList;
  
  public Map<String, Object> toMap() {
    Map<String, Object> metaData = new HashMap<String, Object>();
    metaData.put("head", getHead());
    metaData.put("fieldList", getFieldList());
    metaData.put("dictMap", getDictMap());
    metaData.put("addFieldList", getAddFieldList());
    metaData.put("updateFieldList", getUpdateFieldList());
    metaData.put("btnList", getBtnList());
    metaData.put("topList", getTopList());
    metaData.put("lineList", getLineList());
    metaData.put("jsList", getJsList());
    metaData.put("footerFieldList", getFooterFieldList());
    return metaData;
  }
  
  public ZcurdHead getHead() { return this.head; }
  
  public void setHead(ZcurdHead head) { this.head = head; }
  
  public List<ZcurdField> getFieldList() { return this.fieldList; }
  
  public void setFieldList(List<ZcurdField> fieldList) { this.fieldList = fieldList; }
  
  public Map<String, Map<String, Object>> getDictMap() { return this.dictMap; }
  
  public void setDictMap(Map<String, Map<String, Object>> dictMap) { this.dictMap = dictMap; }
  
  public List<ZcurdField> getAddFieldList() { return this.addFieldList; }
  
  public void setAddFieldList(List<ZcurdField> addFieldList) { this.addFieldList = addFieldList; }
  
  public List<ZcurdField> getUpdateFieldList() { return this.updateFieldList; }
  
  public void setUpdateFieldList(List<ZcurdField> updateFieldList) { this.updateFieldList = updateFieldList; }
  
  public List<ZcurdHeadBtn> getBtnList() { return this.btnList; }
  
  public void setBtnList(List<ZcurdHeadBtn> btnList) { this.btnList = btnList; }
  
  public List<ZcurdHeadJs> getJsList() { return this.jsList; }
  
  public void setJsList(List<ZcurdHeadJs> jsList) { this.jsList = jsList; }
  
  public List<ZcurdHeadBtn> getTopList() { return this.topList; }
  
  public void setTopList(List<ZcurdHeadBtn> topList) { this.topList = topList; }
  
  public List<ZcurdHeadBtn> getLineList() { return this.lineList; }
  
  public void setLineList(List<ZcurdHeadBtn> lineList) { this.lineList = lineList; }
  
  public List<ZcurdField> getFooterFieldList() { return this.footerFieldList; }
  
  public void setFooterFieldList(List<ZcurdField> footerFieldList) { this.footerFieldList = footerFieldList; }
}
