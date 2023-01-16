package com.zcurd.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Duang;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.ICallback;
import com.jfinal.render.FreeMarkerRender;
import com.jfinal.render.RenderException;
import com.zcurd.common.DBTool;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.model.ZcurdField;
import com.zcurd.model.ZcurdHead;
import com.zcurd.service.ZcurdService;
import com.zcurd.vo.ZcurdMeta;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ZcurdHeadController extends BaseController {
	public void list() {
		render("head/list.html");
	}

	public void listData() {
		Object[] queryParams = getQueryParams();
		String[] properties = (String[]) queryParams[0];
		String[] symbols = (String[]) queryParams[1];
		Object[] values = (Object[]) queryParams[2];
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy))
			orderBy = "id desc";
		renderDatagrid(DBTool.findByMultProperties("zcurd_head", properties, symbols, values, orderBy, getPager()),
				DBTool.countByMultProperties("zcurd_head", properties, symbols, values));
	}

	public void updatePage() {
		setAttr("model", ZcurdHead.me.findById(getParaToInt("id")));
		render("head/update.html");
	}

	public void update() {
		String fields = getPara("rowsStr");
		final JSONArray jsonObjs = JSONObject.parseArray(fields);
		Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				ZcurdHead zcurdHead = (ZcurdHead) ZcurdHeadController.this.getModel(ZcurdHead.class, "model");
				zcurdHead.update();
				Long headId = zcurdHead.getLong("id");
				Db.update("delete from zcurd_field where head_id=" + headId);
				if (jsonObjs.size() > 0) {
					for (Object object : jsonObjs) {
						ZcurdField field = new ZcurdField();
						field.set("head_id", headId);
						field.put((Map) object);
						field.save();
					}
				}
				DbMetaTool.updateMetaData(headId.intValue());
				return true;
			}
		});
		addOpLog("[在线表单] 修改");
		renderSuccess("保存成功！");
	}

	public void genFormPage() {
		render("head/genForm.html");
	}

	public void genFormData() {
		String dbSource = getPara("db_source");
		String dbName = (String) DBTool.use(dbSource).execute(new ICallback() {
			public Object call(Connection conn) throws SQLException {
				return conn.getCatalog();
			}
		});
		String sql = "select TABLE_SCHEMA, TABLE_TYPE, a.TABLE_NAME, TABLE_COMMENT, CREATE_TIME from information_schema.TABLES a where a.TABLE_SCHEMA='"
				+ dbName + "' order by CREATE_TIME desc";
		renderDatagrid(DBTool.use(dbSource).find(sql));
	}

	public void genForm() {
		String tableName = getPara("tableName");
		String dbSource = getPara("db_source");
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		zcurdService.genForm(tableName, dbSource);
		addOpLog("[在线表单] 生成表单");
		renderSuccess();
	}

	public void delete() {
		Integer[] ids = getParaValuesToInt("id[]");
		byte b;
		int i;
		Integer[] arrayOfInteger;
		for (i = (arrayOfInteger = ids).length, b = 0; b < i;) {
			Integer id = arrayOfInteger[b];
			ZcurdHead.me.deleteById(id);
			Db.update("delete from zcurd_field where head_id=?", new Object[] { id });
			DbMetaTool.updateMetaData(id.intValue());
			b++;
		}
		addOpLog("[在线表单] 删除");
		renderSuccess();
	}

	public void listField() {
		renderDatagrid(ZcurdField.me.paginate(getParaToInt("page", Integer.valueOf(1)).intValue(),
				getParaToInt("rows", Integer.valueOf(500)).intValue(), getParaToInt("head_id").intValue()));
	}

	public void genCode() throws IOException, TemplateException {
		int headId = getParaToInt("headId").intValue();
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		ZcurdMeta metaMap = zcurdService.getMetaData(headId);
		ZcurdHead head = metaMap.getHead();
		String tableName = head.getTableName();
		String className = String.valueOf(tableName.substring(0, 1).toUpperCase()) + tableName.substring(1);
		int index = className.indexOf("_");
		while (index > 0) {
			String s = className.substring(index + 1, index + 2);
			className = className.replace("_" + s, s.toUpperCase());
			index = className.indexOf("_");
		}
		String lowerClassName = String.valueOf(className.substring(0, 1).toLowerCase()) + className.substring(1);
		Map<String, Object> mateDate = metaMap.toMap();
		mateDate.put("className", className);
		mateDate.put("queryPara", new HashMap());
		copyTemp("listPage.html");
		copyTemp("addPage.html");
		copyTemp("updatePage.html");
		copyTemp("detailPage.html");
		String genCodePath = String.valueOf(PropKit.get("genCodePath")) + className + "/";
		String genCodePagePath = String.valueOf(genCodePath) + lowerClassName + "/";
		(new File(genCodePath)).mkdirs();
		(new File(genCodePagePath)).mkdirs();
		gen(mateDate, "/zcurd/zcurd/genCode/listPage.html", String.valueOf(genCodePagePath) + "list.html");
		gen(mateDate, "/zcurd/zcurd/genCode/addPage.html", String.valueOf(genCodePagePath) + "add.html");
		gen(mateDate, "/zcurd/zcurd/genCode/updatePage.html", String.valueOf(genCodePagePath) + "update.html");
		gen(mateDate, "/zcurd/zcurd/genCode/detailPage.html", String.valueOf(genCodePagePath) + "detail.html");
		copyFile("importExcel.html", genCodePagePath);
		gen(mateDate, "/zcurd/zcurd/genCode/controller.html",
				String.valueOf(genCodePath) + className + "Controller.java");
		gen(mateDate, "/zcurd/zcurd/genCode/model.html", String.valueOf(genCodePath) + className + ".java");
		addOpLog("[在线表单] 生成代码");
		renderSuccess("代码生成成功！保存在" + genCodePath);
	}

	private void gen(Map<String, Object> mateDate, String tempFile, String genFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		Configuration config = FreeMarkerRender.getConfiguration();
		PrintWriter pw = new PrintWriter(new File(genFile), "utf8");
		try {
			Template template = config.getTemplate(tempFile);
			template.process(mateDate, pw);
		} catch (Exception e) {
			throw new RenderException(e);
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	private void copyTemp(String fileName) {
		String basePath = String.valueOf(PathKit.getWebRootPath()) + "/zcurd/zcurd/";
		String content = StringUtil.readTxt2String(new File(String.valueOf(basePath) + fileName));
		content = content.replaceAll("<#include", "\\${\"<\"}#include");
		content = content.replace(
				"<#list item.dict.keySet() as key>,{id:'${key}', text:'${item.dict.get(key)}'}</#list>",
				"<${'#'}list dictData${item.field_name}.keySet() as key>,{id:'${'$'}{key}', text:'${'$'}{dictData${item.field_name}.get(key)}'}</${'#'}list>");
		if ("addPage.html".equals(fileName) || "updatePage.html".equals(fileName)
				|| "detailPage.html".equals(fileName)) {
			content = content.replace("\"headId\": ${headId},", "");
			content = content.replace("model[item.field_name]??", "1==1");
			content = content.replace("${model[head.id_field]}", "${'$'}{model.${head.id_field}}");
			content = content.replaceAll("\\$\\{model\\[item\\.field_name\\]", "\\${'\\$'}{model.\\${item.field_name}");
			content = content.replaceAll("\\$\\{modelDetail\\[item\\.field_name\\]",
					"\\${'\\$'}{modelDetail.\\${item.field_name}");
		}
		StringUtil.saveToFile(String.valueOf(basePath) + "genCode/" + fileName, content);
	}

	private void copyFile(String fileName, String filePath) {
		String basePath = String.valueOf(PathKit.getWebRootPath()) + "/zcurd/zcurd/genCode/";
		String content = StringUtil.readTxt2String(new File(String.valueOf(basePath) + fileName));
		StringUtil.saveToFile(String.valueOf(filePath) + fileName, content);
	}
}
