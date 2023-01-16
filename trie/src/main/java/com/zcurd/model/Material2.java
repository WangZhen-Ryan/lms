package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.CommonUtils;
import java.util.Map;

public class Material2 extends Model<Material2> {
  private static final long serialVersionUID = 1L;
  
  public static final Material2 me = new Material2();
  
  public Map<String, Object> getDictDataCategory() { return DbMetaTool.getDictData(CommonUtils.getDictSql("category")); }
  
  public Map<String, Object> getDictDataLocation() { return DbMetaTool.getDictData("select id as k ,auto_val as v from sys_address_dict  where `status`='1'"); }
  
  public String getDictDataLocation(String code) { return DbMetaTool.getDictData("select id as k ,auto_val as v from sys_address_dict  where `status`='1'", code); }
  
  public Map<String, Object> getDictDataType() { return DbMetaTool.getDictData(CommonUtils.getDictSql("material_name")); }
  
  public Map<String, Object> getDictDataCode() { return DbMetaTool.getDictData(CommonUtils.getDictSql("produce_code")); }
  
  public Map<String, Object> getDictDataStatus() { return DbMetaTool.getDictData(CommonUtils.getDictSql("状态")); }
  
  public Map<String, Object> getDictDataUser() { return DbMetaTool.getDictData("select id as k ,display_name as v from sys_user  where `id` !='6ae3f56fc43c5420417121954607de52'"); }
}
