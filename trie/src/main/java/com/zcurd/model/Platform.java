package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.CommonUtils;
import java.util.Map;

public class Platform extends Model<Platform> {
  private static final long serialVersionUID = 1L;
  
  public static final Platform me = new Platform();
  
  public Map<String, Object> getDictDatalocation() { return DbMetaTool.getDictData("select id as k ,auto_val as v from sys_address_dict  where `status`='1'"); }
  
  public Map<String, Object> getDictDatastatus() { return DbMetaTool.getDictData(CommonUtils.getDictSql("状态")); }
}
