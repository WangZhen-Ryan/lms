package com.zcurd.service;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.zcurd.common.util.StringUtil;

public class RoleService {
	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void saveAuth(String menuIds, String btnIds, String dataruleIds, int roleId) {
		Db.update("delete from sys_role_menu where role_id=?", new Object[] { Integer.valueOf(roleId) });
		if (StringUtil.isNotEmpty(menuIds)) {
			byte b;
			int i;
			String[] arrayOfString;
			for (i = (arrayOfString = menuIds.split(",")).length, b = 0; b < i;) {
				String menuId = arrayOfString[b];
				Db.update("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)",
						new Object[] { Integer.valueOf(roleId), menuId });
				b++;
			}
		}
		Db.update("delete from sys_role_btn where role_id=?", new Object[] { Integer.valueOf(roleId) });
		if (StringUtil.isNotEmpty(btnIds)) {
			byte b;
			int i;
			String[] arrayOfString;
			for (i = (arrayOfString = btnIds.split(",")).length, b = 0; b < i;) {
				String btnId = arrayOfString[b];
				Db.update("INSERT INTO sys_role_btn (role_id, btn_id) VALUES (?, ?)",
						new Object[] { Integer.valueOf(roleId), btnId });
				b++;
			}
		}
		Db.update("delete from sys_role_datarule where role_id=?", new Object[] { Integer.valueOf(roleId) });
		if (StringUtil.isNotEmpty(dataruleIds)) {
			byte b;
			int i;
			String[] arrayOfString;
			for (i = (arrayOfString = dataruleIds.split(",")).length, b = 0; b < i;) {
				String dataruleId = arrayOfString[b];
				Db.update("INSERT INTO sys_role_datarule (role_id, datarule_id) VALUES (?, ?)",
						new Object[] { Integer.valueOf(roleId), dataruleId });
				b++;
			}
		}
	}
}
