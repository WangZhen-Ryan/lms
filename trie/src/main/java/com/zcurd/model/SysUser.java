package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;
import com.zcurd.common.DbMetaTool;
import java.util.List;
import java.util.Map;

public class SysUser extends Model<SysUser> {
	private static final long serialVersionUID = 1L;

	public static final SysUser me = new SysUser();

	public List<SysUser> findByMultiProperties(String[] properties, Object[] values) {
		StringBuffer sql = new StringBuffer("select * from " + getTable().getName() + " where 1=1");
		if (properties != null) {
			byte b;
			int i;
			String[] arrayOfString;
			for (i = (arrayOfString = properties).length, b = 0; b < i;) {
				String property = arrayOfString[b];
				sql.append(" and " + property + "=?");
				b++;
			}

		}
		if (values == null) {
			values = new Object[0];
		}
		return find(sql.toString(), values);
	}

	private Table getTable() {
		return TableMapping.me().getTable(getUsefulClass());
	}

	public Map<String, Object> getDictDataroles() {
		return DbMetaTool.getDictData("select id, role_name from sys_role");
	}

	private Class<? extends Model> getUsefulClass() {
		Class c = getClass();
		return (c.getName().indexOf("EnhancerByCGLIB") == -1) ? c : c.getSuperclass();
	}
}
