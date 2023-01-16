package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import java.util.Map;

public class SysDict extends Model<SysDict> {
  private static final long serialVersionUID = 1L;
  
  public static final SysDict me = new SysDict();
  
  public Map<String, Object> getDictDatadict_type() { return DbMetaTool.getDictData("select distinct dict_type as 'key', dict_type as 'value' from sys_dict"); }
}
