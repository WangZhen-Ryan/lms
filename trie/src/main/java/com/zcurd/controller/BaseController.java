package com.zcurd.controller;

import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.JsonRender;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.Check;
import com.zcurd.common.util.CommonUtils;
import com.zcurd.common.util.Pager;
import com.zcurd.model.MenuDatarule;
import com.zcurd.model.SysOplog;
import com.zcurd.model.SysUser;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BaseController extends Controller {
	protected static final Log logger = Log.getLog(BaseController.class);

	protected String getSearchText(String tableName) {
		String sqlSelect = "";
		String searchText = getPara("searchText");
		if (StringUtils.isNotEmpty(searchText)) {
			List<Record> query = Db.find(
					"select COLUMN_NAME,DATA_TYPE from information_schema.COLUMNS where table_schema='zcurd_base' and table_name = '"
							+ tableName + "'");
			List<String> list = new ArrayList<String>();
			for (int i = 1; i < query.size(); i++) {
				String columnName = ((Record) query.get(i)).getStr("COLUMN_NAME");
				String data_type = ((Record) query.get(i)).getStr("DATA_TYPE");
				if (!columnName.toLowerCase().equals("id"))
					if (data_type.toLowerCase().equals("date") || data_type.toLowerCase().equals("timestamp")
							|| data_type.toLowerCase().equals("datetime")) {
						list.add("date_format(a.`" + columnName + "`,'%Y-%m-%d') like '%" + searchText + "%'");
					} else {
						list.add("a.`" + columnName + "` like '%" + searchText + "%'");
					}
			}
			sqlSelect = String.valueOf(sqlSelect) + StringUtils.join(list.iterator(), " or ");
		}
		return sqlSelect;
	}

	public void addressDic() {
		String project = getPara("name");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + project);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String dictAddressComboboxSql = "select id as k ,auto_val as v from sys_address_dict  where `status`='1'";
		String[] parseSql = DBTool.parseSQL4DbSource(dictAddressComboboxSql);
		List<Record> listRecord = DBTool.use(parseSql[0])
				.find("select 'key', 'text' union all select * from (" + parseSql[1] + ") a");
		for (int i = 1; i < listRecord.size(); i++) {
			Record record = (Record) listRecord.get(i);
			Map<String, Object> datagrid = new HashMap<String, Object>();
			datagrid.put("id", record.getStr("key"));
			datagrid.put("text", record.getStr("text"));
			list.add(datagrid);
		}
		renderJson(list);
	}

	protected void renderDatagrid(Page<?> pageData) {
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pageData.getList());
		datagrid.put("total", Integer.valueOf(pageData.getTotalRow()));
		renderJson(datagrid);
	}

	protected void renderDatagrid(List<?> list, int total) {
		renderDatagrid(list, total, null);
	}

	protected void renderDatagrid(List<?> list, int total, List<Map<String, Object>> footer) {
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", list);
		datagrid.put("total", Integer.valueOf(total));
		if (footer != null && footer.size() > 0)
			datagrid.put("footer", footer);
		renderJson(datagrid);
	}

	protected void renderDatagrid(List<Record> list) {
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", list);
		renderJson(datagrid);
	}

	protected void renderSuccess(String msg) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "success");
		result.put("msg", msg);
		renderJson(result);
	}

	protected void renderSuccess() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "success");
		renderJson(result);
	}

	protected void renderFileUploadJsonSuccess(String msg) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "success");
		result.put("msg", msg);
		render((new JsonRender(result)).forIE());
	}

	protected void renderFileUploadJsonSuccess() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "success");
		render((new JsonRender(result)).forIE());
	}

	protected void renderFailed(String msg) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		result.put("msg", msg);
		renderJson(result);
	}

	protected void renderFailed() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		renderJson(result);
	}

	protected void renderFileUploadJsonFailed(String msg) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		result.put("msg", msg);
		render((new JsonRender(result)).forIE());
	}

	protected void renderFileUploadJsonFailed() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		render((new JsonRender(result)).forIE());
	}

	protected SysUser getSessionUser() {
		return (SysUser) getSessionAttr("sysUser");
	}

	protected Pager getPager() {
		Pager pager = new Pager();
		pager.setPage(getParaToInt("page", Integer.valueOf(0)).intValue());
		pager.setRows(getParaToInt("rows", Integer.valueOf(0)).intValue());
		return pager;
	}

	protected Object[] getQueryParams() {
		List<String> properties = new ArrayList<String>();
		List<String> symbols = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();
		Map<String, String[]> paraMap = getParaMap();
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if (paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String symbol = "=";
				String value = ((String[]) paraMap.get(paraName))[0];
				if (field.startsWith("_start_")) {
					field = field.replaceAll("^_start_", "");
					symbol = ">=";
				} else if (field.startsWith("_end_")) {
					field = field.replaceAll("^_end_", "");
					symbol = "<=";
				}
				if (value.startsWith("*") && value.endsWith("*")) {
					value = "%" + value.substring(1, value.length() - 1) + "%";
					symbol = "like";
				} else if (value.startsWith("*")) {
					value = "%" + value.substring(1);
					symbol = "like";
				} else if (value.endsWith("*")) {
					value = String.valueOf(value.substring(0, value.length() - 1)) + "%";
					symbol = "like";
				}
				properties.add(field);
				symbols.add(symbol);
				values.add(value);
			}
		}
		if (getAttr("menuDataruleList") != null) {
			List<MenuDatarule> menuDataruleList = (List) getAttr("menuDataruleList");
			for (MenuDatarule menuDatarule : menuDataruleList) {
				properties.add(menuDatarule.getFieldName());
				symbols.add(menuDatarule.getSymbol());
				values.add(menuDatarule.getValue());
			}
		}
		return new Object[] { properties.toArray(new String[0]), symbols.toArray(new String[0]),
				values.toArray(new Object[0]) };
	}

	protected String getOrderBy() {
		String sqlOrderBy = "";
		Map<String, String[]> paraMap = getParaMap();
		if (paraMap.get("sort") != null && ((String[]) paraMap.get("sort")).length > 0) {
			String[] sort = (String[]) paraMap.get("sort")[0].split(",");
			String[] order = (String[]) paraMap.get("order")[0].split(",");
			sqlOrderBy = String.valueOf(sort[0]) + " " + order[0];
			for (int i = 1; i < sort.length; i++)
				sqlOrderBy = String.valueOf(sqlOrderBy) + ", " + sort[i] + " " + order[i];
		}
		return sqlOrderBy;
	}

	protected void addOpLog(String opContent) {
		((SysOplog) ((SysOplog) ((SysOplog) ((SysOplog) ((SysOplog) SysOplog.me.remove("id")).set("user_id",
				getSessionUser().get("id"))).set("op_content", opContent)).set("ip", getRemoteAddress()))
						.set("create_time", new Date())).save();
	}

	protected String getRemoteAddress() {
		String ip = getRequest().getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown"))
			ip = getRequest().getHeader("Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown"))
			ip = getRequest().getHeader("WL-Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown"))
			ip = getRequest().getRemoteAddr();
		return ip;
	}

	public void importExcelPage() {
		render("importExcel.html");
	}

	public void downloadFile() {
		String fileName = getPara("fileName");
		String webRootPath = PathKit.getWebRootPath();
		String filePath = String.valueOf(webRootPath) + File.separator + "res" + File.separator + "tplexcel"
				+ File.separator + fileName + ".xlsx";
		File file = new File(filePath);
		renderFile(file);
	}

	protected String getDictSql(String type) {
		return CommonUtils.getDictSql(type);
	}

	public String getPara(String name) {
		String value = super.getPara(name);
		if (value == null || value.length() <= 0)
			return value;
		return value.replaceAll("'", "''");
	}

	protected String escapeCode(String code) {
		if (!Check.IsStringNULL(code)) {
			if (code.equals("2"))
				return "是";
			return "否";
		}
		return "";
	}

	protected String isNull(Object obejct) {
		if (obejct != null)
			return obejct.toString();
		return "";
	}

	protected String appointmentTime(Object obejct) {
		if (obejct != null) {
			String sobejct = obejct.toString();
			if (sobejct.length() > 16)
				return sobejct.substring(0, 16);
			return sobejct;
		}
		return "";
	}
}
