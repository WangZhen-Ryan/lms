package com.zcurd.service;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.ICallback;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.zcurd.common.DBTool;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.ZcurdTool;
import com.zcurd.common.util.FreemarkUtil;
import com.zcurd.common.util.StringUtil;
import com.zcurd.model.SysUser;
import com.zcurd.model.ZcurdField;
import com.zcurd.model.ZcurdHead;
import com.zcurd.vo.ZcurdMeta;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class ZcurdService {
	public void add(int headId, Map<String, String[]> paraMap, HttpServletRequest request, SysUser user) {
		ZcurdMeta mapmeta = getMetaData(headId);
		ZcurdHead head = mapmeta.getHead();
		List<ZcurdField> addFieldList = mapmeta.getAddFieldList();
		Record record = new Record();
		for (ZcurdField field : addFieldList) {
			String[] paramValues = (String[]) paraMap.get("model." + field.getStr("field_name"));
			record.set(field.getStr("field_name"), (paramValues == null) ? null : paramValues[0]);
		}
		Map<String, Object> varData = new HashMap<String, Object>();
		varData.put("user", user);
		varData.put("metaData", mapmeta);
		varData.put("request", request);
		varData.put("session", request.getSession());
		for (ZcurdField field : mapmeta.getFieldList()) {
			String defaultValue = field.getStr("default_value");
			if (StringUtil.isNotEmpty(defaultValue) && StringUtil.isEmpty(record.getStr(field.getStr("field_name"))))
				record.set(field.getStr("field_name"), FreemarkUtil.parse(defaultValue, varData));
		}
		Db.use(ZcurdTool.getDbSource(head.getDbSource())).save(head.getStr("table_name"), head.getStr("id_field"),
				record);
	}

	public void update(int headId, int id, Map<String, String[]> paraMap) {
		ZcurdMeta mapmeta = getMetaData(headId);
		ZcurdHead head = mapmeta.getHead();
		List<ZcurdField> updateFieldList = mapmeta.getUpdateFieldList();
		Record record = get(headId, id);
		for (ZcurdField field : updateFieldList) {
			if (field.getInt("is_allow_update").intValue() == 1) {
				String[] paramValues = (String[]) paraMap.get("model." + field.getStr("field_name"));
				record.set(field.getStr("field_name"), (paramValues == null) ? null : paramValues[0]);
			}
		}
		Db.use(ZcurdTool.getDbSource(head.getDbSource())).update(head.getStr("table_name"), head.getStr("id_field"),
				record);
	}

	public void delete(int headId, Integer[] ids) {
		ZcurdMeta mapmeta = getMetaData(headId);
		ZcurdHead head = mapmeta.getHead();
		byte b;
		int i;
		Integer[] arrayOfInteger;
		for (i = (arrayOfInteger = ids).length, b = 0; b < i;) {
			Integer id = arrayOfInteger[b];
			Db.use(ZcurdTool.getDbSource(head.getDbSource())).deleteById(head.getStr("table_name"),
					head.getStr("id_field"), new Object[] { id });
			b++;
		}
		DbMetaTool.updateMetaData(headId);
	}

	public List<Map<String, Object>> getFooter(ZcurdMeta mapmeta, String[] properties, String[] symbols,
			Object[] values) {
		List<Map<String, Object>> footer = new ArrayList<Map<String, Object>>();
		ZcurdHead head = mapmeta.getHead();
		List<ZcurdField> footFieldList = mapmeta.getFooterFieldList();
		if (footFieldList.size() > 0) {
			StringBuilder sql = new StringBuilder("select ");
			for (int i = 0; i < footFieldList.size(); i++) {
				ZcurdField field = (ZcurdField) footFieldList.get(i);
				if (i > 0)
					sql.append(",");
				sql.append(" sum(" + field.getStr("field_name") + ")");
			}
			sql.append(" from " + head.getTableName());
			List<Object> list = DBTool.findDbSource(head.getDbSource(), sql.toString(), properties, symbols, values);
			Object[] result = (Object[]) list.get(0);
			Map<String, Object> sumMap = new HashMap<String, Object>();
			for (int i = 0; i < footFieldList.size(); i++)
				sumMap.put(((ZcurdField) footFieldList.get(i)).getStr("field_name"),
						"<span style='color:blue;'>合计：" + result[i] + "</span>");
			footer.add(sumMap);
		}
		return footer;
	}

	public Record get(int headId, int id) {
		ZcurdMeta mapmeta = getMetaData(headId);
		ZcurdHead head = mapmeta.getHead();
		return Db.use(ZcurdTool.getDbSource(head.getDbSource())).findById(head.getStr("table_name"),
				head.getStr("id_field"), new Object[] { Integer.valueOf(id) });
	}

	public ZcurdMeta getMetaData(int headId) {
		return DbMetaTool.getMetaData(headId);
	}

	public ZcurdHead getHead(int headId) {
		return (ZcurdHead) ZcurdHead.me.findById(Integer.valueOf(headId));
	}

	public void genForm(String tableName, String dbSource) {
		final String dbs = ZcurdTool.getDbSource(dbSource);
		Db.use(dbs).execute(new ICallback() {
			public Object call(Connection conn) throws SQLException {
				DatabaseMetaData metaData = conn.getMetaData();
				String dbName = conn.getCatalog();

				ResultSet pkRSet = metaData.getPrimaryKeys(dbName, null, tableName);
				while (pkRSet.next()) {

					String form_name = null;
					try {
						String sql = "select TABLE_COMMENT from information_schema.TABLES a where a.TABLE_SCHEMA=? and a.table_name=?";
						form_name = Db.queryStr(sql, new Object[] { dbName, tableName });
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					if (StringUtil.isEmpty(form_name)) {
						form_name = pkRSet.getString(3);
					}

					ZcurdHead head = (ZcurdHead) ((ZcurdHead) ((ZcurdHead) ((ZcurdHead) (new ZcurdHead())
							.set("table_name", pkRSet.getObject(3))).set("form_name", form_name)).set("id_field",
									pkRSet.getObject(4))).set("db_source", dbs);
					head.save();

					ResultSet colRet = metaData.getColumns(dbName, "%", tableName, "%");
					int orderNum = 2;
					while (colRet.next()) {
						String field_name = colRet.getString("COLUMN_NAME");
						String column_name = colRet.getString("REMARKS");
						if (StringUtil.isEmpty(column_name)) {
							column_name = field_name;
						}

						ZcurdField field = (ZcurdField) ((ZcurdField) ((ZcurdField) ((ZcurdField) ((ZcurdField) ((ZcurdField) (new ZcurdField())
								.set("head_id", Integer.valueOf(head.getLong("id").intValue()))).set("field_name",
										field_name)).set("column_name", column_name)).set("data_type",
												colRet.getString("TYPE_NAME").toLowerCase())).set("order_num",
														Integer.valueOf(orderNum))).set("is_allow_null",
																Integer.valueOf(colRet.getInt("NULLABLE")));
						orderNum++;

						if (field_name.equals(head.getIdField())) {
							field.set("order_num", Integer.valueOf(1));
							orderNum--;
						}

						String dataType = field.getStr("data_type");
						String inputType = "easyui-textbox";
						if (dataType.equals("timestamp") || dataType.equals("date") || dataType.equals("datetime")) {
							inputType = "easyui-datebox";
						} else if (dataType.equals("text")) {
							inputType = "textarea";
						} else if (dataType.endsWith("int") || dataType.equals("long")) {
							inputType = "easyui-numberspinner";
						}
						field.set("input_type", inputType);
						field.save();
					}
				}
				return null;
			}
		});
	}

//	public static void main(String[] args) {
//		C3p0Plugin c3p0Plugin = new C3p0Plugin(
//				"jdbc:mysql://127.0.0.1/zcurd?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", "root",
//				"123456");
//		ActiveRecordPlugin arp = new ActiveRecordPlugin("zcurd", c3p0Plugin);
//		c3p0Plugin.start();
//		arp.start();
//		String tableName = "blog";
//		Db.use("zcurd").execute(new Object());
//	}

	public static void main(String[] args) {
		C3p0Plugin c3p0Plugin = new C3p0Plugin(
				"jdbc:mysql://127.0.0.1/zcurd?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", "root",
				"123456");
		ActiveRecordPlugin arp = new ActiveRecordPlugin("zcurd", (IDataSourceProvider) c3p0Plugin);
		c3p0Plugin.start();
		arp.start();

		String tableName = "blog";
		Db.use("zcurd").execute(new ICallback() {
			public Object call(Connection conn) throws SQLException {
				DatabaseMetaData metaData = conn.getMetaData();
				System.out.println("数据库: " + conn.getCatalog());

				ResultSet tableSet = metaData.getTables(null, "%", "%", new String[] { "TABLE" });
				while (tableSet.next()) {
					System.out.println(
							String.valueOf(tableSet.getString("TABLE_NAME")) + "\t" + tableSet.getString("REMARKS"));
				}

				ResultSet colRet = metaData.getColumns(null, "%", "blog", "%");
				while (colRet.next()) {
					String columnName = colRet.getString("COLUMN_NAME");
					String columnType = colRet.getString("TYPE_NAME");
					int datasize = colRet.getInt("COLUMN_SIZE");
					int digits = colRet.getInt("DECIMAL_DIGITS");
					int nullable = colRet.getInt("NULLABLE");
					System.out.println("字段: " + columnName + "\t" + columnType + "\t" + datasize + "\t" + digits + "\t"
							+ nullable + "\t" + colRet.getString("REMARKS"));
				}

				ResultSet pkRSet = metaData.getPrimaryKeys(null, null, "blog");
				while (pkRSet.next()) {
					System.err.println("****** Comment ******");
					System.err.println("TABLE_CAT : " + pkRSet.getObject(1));
					System.err.println("TABLE_SCHEM: " + pkRSet.getObject(2));
					System.err.println("TABLE_NAME : " + pkRSet.getObject(3));
					System.err.println("COLUMN_NAME: " + pkRSet.getObject(4));
					System.err.println("KEY_SEQ : " + pkRSet.getObject(5));
					System.err.println("PK_NAME : " + pkRSet.getObject(6));
					System.err.println("****** ******* ******");
				}

				return null;
			}
		});
	}
}
