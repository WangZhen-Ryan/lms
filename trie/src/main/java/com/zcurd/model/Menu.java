package com.zcurd.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class Menu extends Model<Menu> {
  private static final long serialVersionUID = 1L;
  
  public static final Menu me = new Menu();
  
  private List<Menu> subMenuList = new ArrayList();
  
  public List<Menu> findAll() { return find("select * from sys_menu order by order_num asc , create_time desc"); }
  
  public List<Menu> findByUser(SysUser user) {
    List<Menu> result = new ArrayList<Menu>();
    String id = user.getStr("id");
    Record findFirst = Db.findFirst("select * from sys_user_role where id = ?", new Object[] { id });
    if (findFirst != null && StringUtils.isNotEmpty(findFirst.getStr("roles")))
      result = find(
          "select distinct b.* from sys_role_menu a join sys_menu b on a.menu_id=b.id where a.role_id in(" + 
          findFirst.getStr("roles") + ") order by b.order_num asc"); 
    return result;
  }
  
  public List<Menu> getSubMenuList() { return this.subMenuList; }
  
  public void setSubMenuList(List<Menu> subMenuList) { this.subMenuList = subMenuList; }
}
