package com.zcurd.common;

import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.util.StringUtil;
import com.zcurd.model.ZcurdField;
import com.zcurd.model.ZcurdHead;
import com.zcurd.model.ZcurdHeadBtn;
import com.zcurd.model.ZcurdHeadJs;
import com.zcurd.vo.ZcurdMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DbMetaTool {
  private static Map<Integer, ZcurdMeta> metaDataMap = new Hashtable();
  
  public static ZcurdMeta getMetaData(int headId) {
    ZcurdMeta metaData = (ZcurdMeta)metaDataMap.get(Integer.valueOf(headId));
    if (metaData == null) {
      metaData = getMetaDataFromDb(headId);
      metaDataMap.put(Integer.valueOf(headId), metaData);
    } 
    return metaData;
  }
  
  private static ZcurdMeta getMetaDataFromDb(int headId) {
    ZcurdHead head = (ZcurdHead)ZcurdHead.me.findById(Integer.valueOf(headId));
    List<ZcurdField> fieldList = ZcurdField.me.findByHeadId(head.getLong("id").intValue());
    Map<String, Map<String, Object>> dictMap = new HashMap<String, Map<String, Object>>();
    for (ZcurdField zcurdField : fieldList) {
      String dictSql = zcurdField.getStr("dict_sql");
      if (StringUtil.isNotEmpty(dictSql)) {
        Map<String, Object> dictData = getDictData(dictSql);
        dictMap.put(zcurdField.getStr("field_name"), dictData);
        zcurdField.put("dict", dictData);
      } 
    } 
    List<ZcurdField> addFieldList = new ArrayList<ZcurdField>();
    List<ZcurdField> updateFieldList = new ArrayList<ZcurdField>();
    List<ZcurdField> footerFieldList = new ArrayList<ZcurdField>();
    for (ZcurdField zcurdField : fieldList) {
      if (zcurdField.getIsShowList() == 1 && zcurdField.getIsSum() == 1)
        footerFieldList.add(zcurdField); 
      if (!zcurdField.getStr("field_name").equals(head.getStr("id_field"))) {
        if (zcurdField.getInt("is_allow_add").intValue() == 1)
          addFieldList.add(zcurdField); 
        if (zcurdField.getInt("is_allow_update").intValue() == 1 || zcurdField.getInt("is_allow_detail").intValue() == 1)
          updateFieldList.add(zcurdField); 
      } 
    } 
    List<ZcurdHeadBtn> btnList = ZcurdHeadBtn.me.findByHeadId(headId);
    List<ZcurdHeadBtn> topList = new ArrayList<ZcurdHeadBtn>();
    List<ZcurdHeadBtn> lineList = new ArrayList<ZcurdHeadBtn>();
    for (ZcurdHeadBtn btn : btnList) {
      if (btn.getInt("location").intValue() == 1) {
        topList.add(btn);
        continue;
      } 
      if (btn.getInt("location").intValue() == 2)
        lineList.add(btn); 
    } 
    for (ZcurdHeadBtn btn : btnList) {
      if (btn.getInt("action").intValue() == 1) {
        head.set("form_type", Integer.valueOf(2));
        break;
      } 
    } 
    List<ZcurdHeadJs> jsList = ZcurdHeadJs.me.findByHeadId(headId);
    ZcurdMeta zcurdMeta = new ZcurdMeta();
    zcurdMeta.setHead(head);
    zcurdMeta.setFieldList(fieldList);
    zcurdMeta.setDictMap(dictMap);
    zcurdMeta.setAddFieldList(addFieldList);
    zcurdMeta.setUpdateFieldList(updateFieldList);
    zcurdMeta.setFooterFieldList(footerFieldList);
    zcurdMeta.setBtnList(btnList);
    zcurdMeta.setTopList(topList);
    zcurdMeta.setLineList(lineList);
    zcurdMeta.setJsList(jsList);
    return zcurdMeta;
  }
  
  public static void updateMetaData(int headId) { metaDataMap.remove(Integer.valueOf(headId)); }
  
  public static Map<String, Object> getDictData(String dictSql) {
    String[] parseSql = DBTool.parseSQL4DbSource(dictSql);
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    List<Record> listRecord = DBTool.use(parseSql[0]).find("select 'key', 'text' union all select * from (" + parseSql[1] + ") a");
    for (int i = 1; i < listRecord.size(); i++) {
      Record record = (Record)listRecord.get(i);
      String key = record.getStr("key");
      String text = record.getStr("text");
      key = record.getStr("key").replaceAll("\"", "”").replaceAll("'", "\\\\'");
      text = record.getStr("text").replaceAll("\"", "”").replaceAll("'", "\\\\'");
      map.put(key, text);
    } 
    return map;
  }
  
  public static String getDictData(String dictSql, String code) {
    String[] parseSql = DBTool.parseSQL4DbSource(dictSql);
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    List<Record> listRecord = DBTool.use(parseSql[0]).find("select 'key', 'text' union all select * from (" + parseSql[1] + " and `id`='" + code + "') a");
    int i = 1;
    if (i < listRecord.size()) {
      Record record = (Record)listRecord.get(i);
      return record.getStr("text").replaceAll("\"", "”").replaceAll("'", "\\\\'");
    } 
    return null;
  }
}
