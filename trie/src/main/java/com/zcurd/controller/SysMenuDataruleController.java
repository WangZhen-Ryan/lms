package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.ext.render.csv.CsvRender;
import com.zcurd.model.MenuDatarule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SysMenuDataruleController extends BaseController {
	public void listPage() {
		setAttr("dictDatamenu_id", MenuDatarule.me.getDictDatamenu_id());
		setAttr("dictDatasymbol", MenuDatarule.me.getDictDatasymbol());
		setAttr("menu_id", getPara("menu_id"));
		render("list.html");
	}

	public void listData() {
		Object[] queryParams = getQueryParams();
		String[] properties = (String[]) queryParams[0];
		String[] symbols = (String[]) queryParams[1];
		Object[] values = (Object[]) queryParams[2];
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy))
			orderBy = "id desc";
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_menu_datarule", properties, symbols,
				values, orderBy, getPager());
		Map<String, Object> dictDatamenu_id = MenuDatarule.me.getDictDatamenu_id();
		for (Record record : list) {
			String fieldName = "menu_id";
			if (record.get(fieldName) != null && dictDatamenu_id.get(record.get(fieldName).toString()) != null)
				record.set(fieldName, dictDatamenu_id.get(record.get(fieldName).toString()));
		}
		Map<String, Object> dictDatasymbol = MenuDatarule.me.getDictDatasymbol();
		for (Record record : list) {
			String fieldName = "symbol";
			if (record.get(fieldName) != null && dictDatasymbol.get(record.get(fieldName).toString()) != null)
				record.set(fieldName, dictDatasymbol.get(record.get(fieldName).toString()));
		}
		renderDatagrid(list,
				DBTool.countByMultPropertiesDbSource("zcurd_base", "sys_menu_datarule", properties, symbols, values));
	}

	public void addPage() {
		setAttr("dictDatamenu_id", MenuDatarule.me.getDictDatamenu_id());
		setAttr("dictDatasymbol", MenuDatarule.me.getDictDatasymbol());
		setAttr("menu_id", getPara("menu_id"));
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		((MenuDatarule) getModel(MenuDatarule.class, "model")).save();
		addOpLog("[菜单数据（权限编辑）] 增加");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDatamenu_id", MenuDatarule.me.getDictDatamenu_id());
		setAttr("dictDatasymbol", MenuDatarule.me.getDictDatasymbol());
		setAttr("model", MenuDatarule.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		MenuDatarule model = (MenuDatarule) MenuDatarule.me.findById(getPara("id"));
		model.set("menu_id", getPara("model.menu_id"));
		model.set("field_name", getPara("model.field_name"));
		model.set("symbol", getPara("model.symbol"));
		model.set("value", getPara("model.value"));
		model.update();
		addOpLog("[菜单数据（权限编辑）] 修改");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void delete() {
		Integer[] ids = getParaValuesToInt("id[]");
		byte b;
		int i;
		Integer[] arrayOfInteger;
		for (i = (arrayOfInteger = ids).length, b = 0; b < i;) {
			Integer id = arrayOfInteger[b];
			((MenuDatarule) (new MenuDatarule()).set("id", id)).delete();

			b++;
		}
		addOpLog("[菜单数据（权限编辑）] 删除");
		renderSuccess();
	}

	public void detailPage() {
		MenuDatarule model = (MenuDatarule) MenuDatarule.me.findById(getParaToInt("id"));
		Map<String, Object> dictDatamenu_id = MenuDatarule.me.getDictDatamenu_id();
		if (dictDatamenu_id.get(model.get("menu_id").toString()) != null)
			model.set("menu_id", dictDatamenu_id.get(model.get("menu_id").toString()));
		Map<String, Object> dictDatasymbol = MenuDatarule.me.getDictDatasymbol();
		if (dictDatasymbol.get(model.get("symbol").toString()) != null)
			model.set("symbol", dictDatasymbol.get(model.get("symbol").toString()));
		setAttr("model", model);
		render("detail.html");
	}

	public void exportCsv() {
		Object[] queryParams = getQueryParams();
		String[] properties = (String[]) queryParams[0];
		String[] symbols = (String[]) queryParams[1];
		Object[] values = (Object[]) queryParams[2];
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy))
			orderBy = "id desc";
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_menu_datarule", properties, symbols,
				values);
		Map<String, Object> dictDatamenu_id = MenuDatarule.me.getDictDatamenu_id();
		for (Record record : list) {
			String fieldName = "menu_id";
			if (dictDatamenu_id.get(record.get(fieldName).toString()) != null)
				record.set(fieldName, dictDatamenu_id.get(record.get(fieldName).toString()));
		}
		Map<String, Object> dictDatasymbol = MenuDatarule.me.getDictDatasymbol();
		for (Record record : list) {
			String fieldName = "symbol";
			if (dictDatasymbol.get(record.get(fieldName).toString()) != null)
				record.set(fieldName, dictDatasymbol.get(record.get(fieldName).toString()));
		}
		List<String> headers = new ArrayList<String>();
		List<String> clomuns = new ArrayList<String>();
		headers.add("菜单");
		clomuns.add("menu_id");
		headers.add("字段名称");
		clomuns.add("field_name");
		headers.add("符号");
		clomuns.add("symbol");
		headers.add("字段件值");
		clomuns.add("value");
		CsvRender csvRender = new CsvRender(headers, list);
		csvRender.clomuns(clomuns);
		csvRender.fileName("菜单数据（权限编辑）");
		addOpLog("[菜单数据（权限编辑）] 导出cvs");
		render(csvRender);
	}
}
