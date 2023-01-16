package com.zcurd.controller;

import com.jfinal.aop.Duang;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.DBTool;
import com.zcurd.common.DbMetaTool;
import com.zcurd.common.ZcurdTool;
import com.zcurd.common.handler.CurdHandle;
import com.zcurd.common.util.FreemarkUtil;
import com.zcurd.common.util.StringUtil;
import com.zcurd.ext.render.csv.CsvRender;
import com.zcurd.model.ZcurdField;
import com.zcurd.model.ZcurdHead;
import com.zcurd.model.ZcurdHeadJs;
import com.zcurd.service.ZcurdService;
import com.zcurd.vo.ZcurdMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZcurdController extends BaseController {
	public void listPage() {
		int headId = getHeadId();
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		ZcurdMeta metaData = zcurdService.getMetaData(headId);
		flushDictData(metaData);
		setAttr("headId", Integer.valueOf(headId));
		setAttrs(metaData.toMap());
		setAttr("queryPara", ZcurdTool.getQueryPara(getParaMap()));
		handleVar(metaData, null);
		render("listPage.html");
	}

	public void listData() {
		int headId = getHeadId();
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		ZcurdMeta metaData = DbMetaTool.getMetaData(headId);
		ZcurdHead head = metaData.getHead();
		Object[] queryParams = getQueryParams();
		String[] properties = (String[]) queryParams[0];
		String[] symbols = (String[]) queryParams[1];
		Object[] values = (Object[]) queryParams[2];
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy))
			orderBy = String.valueOf(head.getIdField()) + " desc";
		renderDatagrid(
				ZcurdTool.replaceDict(metaData,
						DBTool.findByMultPropertiesDbSource(head.getDbSource(), head.getTableName(), properties,
								symbols, values, orderBy, getPager())),
				DBTool.countByMultPropertiesDbSource(head.getDbSource(), head.getTableName(), properties, symbols,
						values),
				zcurdService.getFooter(metaData, properties, symbols, values));
	}

	public void addPage() {
		int headId = getHeadId();
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		ZcurdMeta metaData = zcurdService.getMetaData(headId);
		flushDictData(metaData);
		Map<String, Object> varData = new HashMap<String, Object>();
		varData.put("user", getSessionUser());
		varData.put("metaData", metaData);
		varData.put("request", getRequest());
		varData.put("session", getRequest().getSession());
		for (ZcurdField field : metaData.getAddFieldList()) {
			String defaultValue = field.getStr("default_value");
			if (StringUtil.isNotEmpty(defaultValue))
				field.set("default_value", FreemarkUtil.parse(defaultValue, varData));
		}
		setAttr("headId", Integer.valueOf(headId));
		setAttrs(metaData.toMap());
		setAttr("queryPara", ZcurdTool.getQueryPara(getParaMap()));
		handleVar(metaData, null);
	}

	public void add() {
		int headId = getHeadId();
		ZcurdMeta metaData = DbMetaTool.getMetaData(headId);
		ZcurdHead head = metaData.getHead();
		Map<String, String[]> paraMap = new HashMap<String, String[]>();
		paraMap.putAll(getParaMap());
		String handleClass = head.getStr("handle_class");
		if (StringUtil.isNotEmpty(handleClass))
			try {
				CurdHandle ch = (CurdHandle) Duang.duang(Class.forName(handleClass));
				ch.add(metaData, getRequest(), paraMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		zcurdService.add(getHeadId(), paraMap, getRequest(), getSessionUser());
		addOpLog("[" + head.getFormName() + "] 增加");
		renderSuccess();
	}

	public void updatePage() {
		int headId = getHeadId();
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		ZcurdMeta metaData = zcurdService.getMetaData(headId);
		flushDictData(metaData);
		setAttr("headId", Integer.valueOf(headId));
		setAttrs(metaData.toMap());
		Record currRecord = zcurdService.get(headId, getParaToInt("id").intValue());
		setAttr("model", currRecord.getColumns());
		handleVar(metaData, currRecord);
		render("updatePage.html");
	}

	public void update() {
		int headId = getHeadId();
		ZcurdMeta metaData = DbMetaTool.getMetaData(headId);
		ZcurdHead head = metaData.getHead();
		Map<String, String[]> paraMap = new HashMap<String, String[]>();
		paraMap.putAll(getParaMap());
		String handleClass = head.getStr("handle_class");
		if (StringUtil.isNotEmpty(handleClass))
			try {
				CurdHandle ch = (CurdHandle) Duang.duang(Class.forName(handleClass));
				ch.update(metaData, getRequest(), paraMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		zcurdService.update(getHeadId(), getParaToInt("id").intValue(), paraMap);
		addOpLog("[" + head.getFormName() + "] 修改");
		renderSuccess();
	}

	public void delete() {
		int headId = getHeadId();
		ZcurdMeta metaData = DbMetaTool.getMetaData(headId);
		ZcurdHead head = metaData.getHead();
		Integer[] ids = getParaValuesToInt("id[]");
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		zcurdService.delete(getHeadId(), ids);
		addOpLog("[" + head.getFormName() + "] 删除");
		renderSuccess();
	}

	public void detailPage() {
		int headId = getHeadId();
		ZcurdMeta metaData = DbMetaTool.getMetaData(headId);
		ZcurdService zcurdService = (ZcurdService) Duang.duang(ZcurdService.class);
		Record row = zcurdService.get(headId, getParaToInt("id").intValue());
		setAttr("headId", Integer.valueOf(headId));
		setAttrs(metaData.toMap());
		setAttr("model", ZcurdTool.replaceDict(metaData, row));
		handleVar(metaData, row);
		render("detailPage.html");
	}

	public void exportCsv() {
		int headId = getHeadId();
		ZcurdMeta metaData = DbMetaTool.getMetaData(headId);
		ZcurdHead head = metaData.getHead();
		List<ZcurdField> fieldList = metaData.getFieldList();
		flushDictData(metaData);
		Object[] queryParams = getQueryParams();
		String[] properties = (String[]) queryParams[0];
		String[] symbols = (String[]) queryParams[1];
		Object[] values = (Object[]) queryParams[2];
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy))
			orderBy = String.valueOf(head.getIdField()) + " desc";
		List<Record> list = ZcurdTool.replaceDict(headId, DBTool.findByMultPropertiesDbSource(
				ZcurdTool.getDbSource(head.getDbSource()), head.getTableName(), properties, symbols, values));
		List<String> headers = new ArrayList<String>();
		List<String> clomuns = new ArrayList<String>();
		for (ZcurdField zcurdField : fieldList) {
			if (zcurdField.getInt("is_show_list").intValue() == 1) {
				headers.add(zcurdField.getStr("column_name"));
				clomuns.add(zcurdField.getStr("field_name"));
			}
		}
		CsvRender csvRender = new CsvRender(headers, list);
		csvRender.clomuns(clomuns);
		csvRender.fileName(head.getStr("form_name"));
		addOpLog("[" + head.getFormName() + "] 导出cvs");
		render(csvRender);
	}

	private int getHeadId() {
		String headId = (String) getAttr("headId");
		return Integer.parseInt(headId);
	}

	private void flushDictData(ZcurdMeta metaData) {
		for (ZcurdField zcurdField : metaData.getFieldList()) {
			String dictSql = zcurdField.getStr("dict_sql");
			if (StringUtil.isNotEmpty(dictSql)) {
				Map<String, Object> dictData = DbMetaTool.getDictData(dictSql);
				metaData.getDictMap().put(zcurdField.getStr("field_name"), dictData);
				zcurdField.put("dict", dictData);
			}
		}
	}

	private void handleVar(ZcurdMeta metaData, Record currRecord) {
		Map<String, Object> varData = new HashMap<String, Object>();
		varData.put("currRecord", currRecord);
		varData.put("user", getSessionUser());
		varData.put("metaData", metaData);
		varData.put("request", getRequest());
		varData.put("session", getSession());
		List<Object> sqlData = new ArrayList<Object>();
		for (ZcurdHeadJs zcurdHeadJs : metaData.getJsList()) {
			String sqlContent = FreemarkUtil.parse(zcurdHeadJs.getStr("sql_content"), varData);
			if (StringUtil.isNotEmpty(sqlContent)) {
				zcurdHeadJs.set("sql_content", sqlContent);
				byte b;
				int i;
				String[] arrayOfString;
				for (i = (arrayOfString = sqlContent.split(";")).length, b = 0; b < i;) {
					String sql = arrayOfString[b];
					sqlData.add(DBTool.findBySQL4DbSource(sql));
					b++;
				}
			}
		}
		varData.put("sqlData", sqlData);
		for (ZcurdHeadJs zcurdHeadJs : metaData.getJsList())
			zcurdHeadJs.set("js_content", FreemarkUtil.parse(zcurdHeadJs.getStr("js_content"), varData));
	}
}
