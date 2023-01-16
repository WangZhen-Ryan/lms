package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.ext.render.csv.CsvRender;
import com.zcurd.model.MenuBtn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SysMenuBtnController extends BaseController {
	public void listPage() {
		setAttr("dictDatamenu_id", MenuBtn.me.getDictDatamenu_id());
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
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_menu_btn", properties, symbols,
				values, orderBy, getPager());
		Map<String, Object> dictDatamenu_id = MenuBtn.me.getDictDatamenu_id();
		for (Record record : list) {
			String fieldName = "menu_id";
			if (record.get(fieldName) != null && dictDatamenu_id.get(record.get(fieldName).toString()) != null)
				record.set(fieldName, dictDatamenu_id.get(record.get(fieldName).toString()));
		}
		renderDatagrid(list,
				DBTool.countByMultPropertiesDbSource("zcurd_base", "sys_menu_btn", properties, symbols, values));
	}

	public void addPage() {
		setAttr("dictDatamenu_id", MenuBtn.me.getDictDatamenu_id());
		setAttr("menu_id", getPara("menu_id"));
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		((MenuBtn) getModel(MenuBtn.class, "model")).save();
		addOpLog("[菜单按钮（权限编辑）] 增加");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDatamenu_id", MenuBtn.me.getDictDatamenu_id());
		setAttr("model", MenuBtn.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		MenuBtn model = (MenuBtn) MenuBtn.me.findById(getPara("id"));
		model.set("menu_id", getPara("model.menu_id"));
		model.set("btn_name", getPara("model.btn_name"));
		model.set("class_name", getPara("model.class_name"));
		model.set("method_name", getPara("model.method_name"));
		model.update();
		addOpLog("[菜单按钮（权限编辑）] 修改");
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
			((MenuBtn) (new MenuBtn()).set("id", id)).delete();

			b++;
		}
		addOpLog("[菜单按钮（权限编辑）] 删除");
		renderSuccess();
	}

	public void detailPage() {
		MenuBtn model = (MenuBtn) MenuBtn.me.findById(getParaToInt("id"));
		Map<String, Object> dictDatamenu_id = MenuBtn.me.getDictDatamenu_id();
		if (dictDatamenu_id.get(model.get("menu_id").toString()) != null)
			model.set("menu_id", dictDatamenu_id.get(model.get("menu_id").toString()));
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
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_menu_btn", properties, symbols,
				values);
		Map<String, Object> dictDatamenu_id = MenuBtn.me.getDictDatamenu_id();
		for (Record record : list) {
			String fieldName = "menu_id";
			if (dictDatamenu_id.get(record.get(fieldName).toString()) != null)
				record.set(fieldName, dictDatamenu_id.get(record.get(fieldName).toString()));
		}
		List<String> headers = new ArrayList<String>();
		List<String> clomuns = new ArrayList<String>();
		headers.add("所属菜单");
		clomuns.add("menu_id");
		headers.add("按钮名称");
		clomuns.add("btn_name");
		headers.add("页面class名称");
		clomuns.add("class_name");
		headers.add("后台method名称");
		clomuns.add("method_name");
		CsvRender csvRender = new CsvRender(headers, list);
		csvRender.clomuns(clomuns);
		csvRender.fileName("菜单按钮（权限编辑）");
		addOpLog("[菜单按钮（权限编辑）] 导出cvs");
		render(csvRender);
	}
}
