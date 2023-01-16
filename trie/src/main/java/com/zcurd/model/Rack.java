package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.CommonUtils;
import java.util.Map;

public class Rack extends Model<Rack> {
  private static final long serialVersionUID = 1L;
  
  public static final Rack me = new Rack();
  
  public Map<String, Object> getDictDatalocation() { return DbMetaTool.getDictData("select id as k ,auto_val as v from sys_address_dict  where `status`='1'"); }
  
  public Map<String, Object> getDictDataposition() { return DbMetaTool.getDictData(CommonUtils.getDictSql("机架位")); }
}
