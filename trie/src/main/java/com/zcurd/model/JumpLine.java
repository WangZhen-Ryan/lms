package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.CommonUtils;
import java.util.Map;

public class JumpLine extends Model<JumpLine> {
  private static final long serialVersionUID = 1L;
  
  public static final JumpLine me = new JumpLine();
  
  public Map<String, Object> getDictDatalocation() { return DbMetaTool.getDictData("select id as k ,auto_val as v from sys_address_dict  where `status`='1'"); }
  
  public Map<String, Object> getDictDatatype() { return DbMetaTool.getDictData(CommonUtils.getDictSql("jump_line_type")); }
}
