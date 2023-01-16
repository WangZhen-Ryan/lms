package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuDatarule extends Model<MenuDatarule> {
  private static final long serialVersionUID = 1L;
  
  public static final MenuDatarule me = new MenuDatarule();
  
  public Map<String, Object> getDictDatamenu_id() { return DbMetaTool.getDictData("select id, menu_name from sys_menu"); }
  
  public Map<String, Object> getDictDatasymbol() { return DbMetaTool.getDictData("select dict_key, dict_value from sys_dict where dict_type='datarule_symbol'"); }
  
  public List<MenuDatarule> findAll() { return find("select * from sys_menu_datarule"); }
  
  public List<MenuDatarule> findByUser(SysUser user) {
    List<MenuDatarule> result = new ArrayList<MenuDatarule>();
    String roles = user.getStr("roles");
    if (StringUtil.isNotEmpty(roles))
      result = find(
          "select distinct b.* from sys_role_datarule a join sys_menu_datarule b on a.datarule_id=b.id where a.role_id in(" + 
          roles + ")"); 
    return result;
  }
  
  public String getFieldName() { return (String)get("field_name"); }
  
  public String getSymbol() { return (String)get("symbol"); }
  
  public String getValue() { return (String)get("value"); }
}
