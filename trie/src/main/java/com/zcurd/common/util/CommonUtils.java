package com.zcurd.common.util;

import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Record;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class CommonUtils {
  public static Map<String, String> recordListToMap(List<Record> find) {
    Map<String, String> map = new HashMap<String, String>();
    if (find == null || find.isEmpty())
      return map; 
    for (int i = 0; i < find.size(); i++) {
      Record record = (Record)find.get(i);
      map.put(record.getStr("v"), record.getStr("k"));
    } 
    return map;
  }
  
  public static boolean createDir(String destDirName) {
    File dir = new File(destDirName);
    if (dir.exists()) {
      LogKit.info("创建目录" + destDirName + "失败，目标目录已经存在");
      return true;
    } 
    if (!destDirName.endsWith(File.separator))
      destDirName = String.valueOf(destDirName) + File.separator; 
    if (dir.mkdirs()) {
      LogKit.info("创建目录" + destDirName + "成功！");
      return true;
    } 
    LogKit.info("创建目录" + destDirName + "失败！");
    return false;
  }
  
  public static <T extends com.jfinal.plugin.activerecord.Model<T>> void setFieldValue(String fieldName, Map<String, Object> map, T model) {
    if (model.get(fieldName) != null && map.get(model.get(fieldName).toString()) != null)
      model.set(fieldName, map.get(model.get(fieldName).toString())); 
  }
  
  public static <T extends com.jfinal.plugin.activerecord.Model<T>> void setFieldValue(String fieldName, String fieldName_id, Map<String, Object> map, T model) {
    if (Check.IsStringNULL((String)model.get(fieldName))) {
      model.set(fieldName, "");
      model.set(fieldName_id, "");
    } else if (map.get(model.get(fieldName).toString()) != null) {
      model.set(fieldName_id, model.get(fieldName));
      model.set(fieldName, map.get(model.get(fieldName).toString()));
    } else {
      model.set(fieldName_id, "");
    } 
  }
  
  public static void setFieldValue(String fieldName, Map<String, Object> map, Record model) {
    if (model.get(fieldName) != null && map.get(model.get(fieldName).toString()) != null)
      model.set(fieldName, map.get(model.get(fieldName).toString())); 
  }
  
  public static String getDictSql(String type) { return StringUtils.replace("select dict_key as k,dict_value as v from sys_dict where dict_type='#1'", "#1", type); }
}
