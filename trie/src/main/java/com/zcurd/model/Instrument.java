package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.CommonUtils;
import java.util.Map;

public class Instrument extends Model<Instrument> {
  private static final long serialVersionUID = 1L;
  
  public static final Instrument me = new Instrument();
  
  public Map<String, Object> getDictDatalocation() { return DbMetaTool.getDictData("select id as k ,auto_val as v from sys_address_dict  where `status`='1'"); }
  
  public Map<String, Object> getDictDatastatus() { return DbMetaTool.getDictData(CommonUtils.getDictSql("状态")); }
  
  public Map<String, Object> getDictDatamodulemo() { return DbMetaTool.getDictData(CommonUtils.getDictSql("模块规格")); }
  
  public Map<String, Object> getDictDatamoduletype() { return DbMetaTool.getDictData(CommonUtils.getDictSql("模块类型")); }
  
  public Map<String, Object> getDictDatainstrumenttype() { return DbMetaTool.getDictData(CommonUtils.getDictSql("仪表类型")); }
}
