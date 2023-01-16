package com.zcurd.common.handler;

import com.zcurd.common.DbMetaTool;
import com.zcurd.vo.ZcurdMeta;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class FlushFormCurdHandle implements CurdHandle {
  public void add(ZcurdMeta zcurdMeta, HttpServletRequest req, Map<String, String[]> paraMap) {
    String[] headIdArr = (String[])paraMap.get("model.head_id");
    if (headIdArr != null && headIdArr.length > 0) {
      int fHeadId = Integer.parseInt(headIdArr[0]);
      DbMetaTool.updateMetaData(fHeadId);
    } 
  }
  
  public void update(ZcurdMeta zcurdMeta, HttpServletRequest req, Map<String, String[]> paraMap) {
    String[] headIdArr = (String[])paraMap.get("model.head_id");
    if (headIdArr != null && headIdArr.length > 0) {
      int fHeadId = Integer.parseInt(headIdArr[0]);
      DbMetaTool.updateMetaData(fHeadId);
    } 
  }
  
  public void delete(ZcurdMeta zcurdMeta, HttpServletRequest req, Map<String, String[]> paraMap) {}
}
