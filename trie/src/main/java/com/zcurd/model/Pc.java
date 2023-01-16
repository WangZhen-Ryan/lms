package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.CommonUtils;
import java.util.Map;

public class Pc extends Model<Pc> {
  private static final long serialVersionUID = 1L;
  
  public static final Pc me = new Pc();
  
  public Map<String, Object> getDictDataLocation() { return DbMetaTool.getDictData("select id as k ,auto_val as v from sys_address_dict  where `status`='1'"); }
  
  public Map<String, Object> getDictDatapc_property() { return DbMetaTool.getDictData(CommonUtils.getDictSql("PC属性")); }
}
