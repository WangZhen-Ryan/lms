package com.zcurd.common;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.zcurd.common.util.Pager;
import com.zcurd.common.util.StringUtil;
import java.util.List;

public class DBTool {
	public static final DBTool me = new DBTool();

	public static List<Record> findByMultProperties(String table, String[] properties, Object[] values) {
		return findByMultProperties(table, properties, values, null, null);
	}

	public static List<Record> findByMultProperties(String table, String[] properties, Object[] values, Pager pager) {
		return findByMultProperties(table, properties, values, null, pager);
	}

	public static List<Record> findByMultProperties(String table, String[] properties, Object[] values, String orderBy,
			Pager pager) {
		String[] symbols = new String[properties.length];
		for (int i = 0; i < properties.length; i++)
			symbols[i] = "=";
		return findByMultProperties(null, table, properties, symbols, values, orderBy, pager);
	}

	public static List<Record> findByMultProperties(String table, String[] properties, String[] symbols,
			Object[] values) {
		return findByMultProperties(null, table, properties, symbols, values, null, null);
	}

	public static List<Record> findByMultProperties(String table, String[] properties, String[] symbols,
			Object[] values, Pager pager) {
		return findByMultProperties(null, table, properties, symbols, values, null, pager);
	}

	public static List<Record> findByMultProperties(String table, String[] properties, String[] symbols,
			Object[] values, String orderBy, Pager pager) {
		return findByMultProperties(null, table, properties, symbols, values, orderBy, pager);
	}

	public static List<Record> findByMultProperties(String[] fields, String table, String[] properties,
			Object[] values) {
		return findByMultProperties(fields, table, properties, null, values, null, null);
	}

	public static List<Record> findByMultProperties(String[] fields, String table, String[] properties,
			String[] symbols, Object[] values) {
		return findByMultProperties(fields, table, properties, symbols, values, null, null);
	}

	public static List<Record> findByMultProperties(String[] fields, String table, String[] properties,
			String[] symbols, Object[] values, Pager pager) {
		return findByMultProperties(fields, table, properties, symbols, values, null, pager);
	}

	public static List<Record> findByMultProperties(String[] fields, String table, String[] properties,
			String[] symbols, Object[] values, String orderBy, Pager pager) {
		return findByMultPropertiesDbSource(null, fields, table, properties, symbols, values, orderBy, pager);
	}

	public static List<Record> findByMultPropertiesDbSource(String dbSource, String table, String[] properties,
			String[] symbols, Object[] values) {
		return findByMultPropertiesDbSource(dbSource, null, table, properties, symbols, values, null, null);
	}

	public static List<Record> findByMultPropertiesDbSource(String dbSource, String table, String[] properties,
			String[] symbols, Object[] values, Pager pager) {
		return findByMultPropertiesDbSource(dbSource, null, table, properties, symbols, values, null, pager);
	}

	public static List<Record> findByMultPropertiesDbSource(String dbSource, String table, String[] properties,
			String[] symbols, Object[] values, String orderBy, Pager pager) {
		return findByMultPropertiesDbSource(dbSource, null, table, properties, symbols, values, orderBy, pager);
	}

	public static List<Record> findByMultPropertiesDbSource(String dbSource, String[] fields, String table,
			String[] properties, String[] symbols, Object[] values, String orderBy, Pager pager) {
		checkSecurity(properties, symbols);
		StringBuilder sb = new StringBuilder("select ");
		if (fields == null || fields.length == 0)
			fields = new String[] { "*" };
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(fields[i]);
		}
		sb.append(" from " + table + " where 1=1");
		for (int i = 0; i < properties.length; i++)
			sb.append(" and " + properties[i] + " " + symbols[i] + " ?");
		if (StringUtil.isNotEmpty(orderBy))
			sb.append(" order by " + orderBy);
		if (pager != null)
			sb.append(" limit " + pager.getStartRow() + ", " + pager.getRows());
		return Db.use(ZcurdTool.getDbSource(dbSource)).find(sb.toString(), values);
	}

	public static int countByMultProperties(String table, String[] properties, Object[] values) {
		String[] symbols = new String[properties.length];
		for (int i = 0; i < properties.length; i++)
			symbols[i] = "=";
		return countByMultProperties(table, properties, symbols, values);
	}

	public static int countByMultProperties(String table, String[] properties, String[] symbols, Object[] values) {
		return countByMultPropertiesDbSource(null, table, properties, symbols, values);
	}

	public static int countByMultPropertiesDbSource(String dbSource, String table, String[] properties,
			String[] symbols, Object[] values) {
		checkSecurity(properties, symbols);
		StringBuilder sb = new StringBuilder("select count(*)");
		sb.append(" from " + table + " where 1=1");
		for (int i = 0; i < properties.length; i++)
			sb.append(" and " + properties[i] + " " + symbols[i] + " ?");
		return Db.use(ZcurdTool.getDbSource(dbSource)).queryLong(sb.toString(), values).intValue();
	}

	public static List<Object> findDbSource(String dbSource, String selectSQL, String[] properties, String[] symbols,
			Object[] values) {
		checkSecurity(properties, symbols);
		StringBuilder sb = new StringBuilder(selectSQL);
		if (selectSQL.toLowerCase().indexOf("where") < 0)
			sb.append(" where");
		sb.append(" 1=1");
		for (int i = 0; i < properties.length; i++)
			sb.append(" and " + properties[i] + " " + symbols[i] + " ?");
		return Db.use(ZcurdTool.getDbSource(dbSource)).query(sb.toString(), values);
	}

	public static String[] parseSQL4DbSource(String sql) {
		String dbSource = "";
		if (sql.toLowerCase().startsWith("[dbSource".toLowerCase())) {
			dbSource = sql.substring(1, sql.indexOf("]")).split("=")[1].trim();
			sql = sql.substring(sql.indexOf("]") + 1);
		}
		return new String[] { dbSource, sql };
	}

	public static List<Record> findBySQL4DbSource(String sql) {
		String[] parseSql = parseSQL4DbSource(sql);
		return use(parseSql[0]).find(parseSql[1]);
	}

	public static DbPro use(String dbSource) {
		return Db.use(ZcurdTool.getDbSource(dbSource));
	}

	public static boolean isSecurity(String[] properties, String[] symbols) {
		return isSecurity(properties, symbols, false);
	}

	public static boolean checkSecurity(String[] properties, String[] symbols) {
		return isSecurity(properties, symbols, true);
	}

	private static boolean isSecurity(String[] properties, String[] symbols, boolean isThrowException) {
		String[] danger4Symbols;
		String symbol;
		int var5;
		int var6;
		String[] var7;
		String str;
		int var9;
		int var10;
		String[] var11;
		if (properties != null) {
			danger4Symbols = new String[] { " ", "=", ">", "<" };
			var7 = properties;
			var6 = properties.length;

			for (var5 = 0; var5 < var6; ++var5) {
				symbol = var7[var5];
				if (StringUtil.isNotEmpty(symbol)) {
					symbol = symbol.trim();
					var11 = danger4Symbols;
					var10 = danger4Symbols.length;

					for (var9 = 0; var9 < var10; ++var9) {
						str = var11[var9];
						if (symbol.contains(str)) {
							if (isThrowException) {
								throw new RuntimeException("SQL Danger: [" + symbol + "] 不是一个安全的properties");
							}

							return false;
						}
					}
				}
			}
		}

		if (properties != null) {
			danger4Symbols = new String[] { " ", "'" };
			var7 = symbols;
			var6 = symbols.length;

			for (var5 = 0; var5 < var6; ++var5) {
				symbol = var7[var5];
				if (StringUtil.isNotEmpty(symbol)) {
					symbol = symbol.trim();
					var11 = danger4Symbols;
					var10 = danger4Symbols.length;

					for (var9 = 0; var9 < var10; ++var9) {
						str = var11[var9];
						if (symbol.contains(str)) {
							if (isThrowException) {
								throw new RuntimeException("SQL Danger: [" + symbol + "] 不是一个安全的symbols");
							}

							return false;
						}
					}
				}
			}
		}

		return true;
	}

	public static void main(String[] args) {
		C3p0Plugin c3p0Plugin = new C3p0Plugin(
				"jdbc:mysql://127.0.0.1/zcurd?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", "root",
				"123456");
		ActiveRecordPlugin arp = new ActiveRecordPlugin("zcurd", c3p0Plugin);
		c3p0Plugin.start();
		arp.start();
		List<Record> list = findByMultProperties("sys_menu", new String[] { "parent_id" },
				new Object[] { Integer.valueOf(0) });
		for (Record record : list)
			System.out.println(record);
		System.out.println(
				countByMultProperties("sys_menu", new String[] { "parent_id" }, new Object[] { Integer.valueOf(0) }));
		System.out.println(isSecurity(new String[] { "name>1" }, new String[0]));
	}
}
