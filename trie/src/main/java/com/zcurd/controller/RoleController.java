package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.ext.render.csv.CsvRender;
import com.zcurd.model.Menu;
import com.zcurd.model.MenuBtn;
import com.zcurd.model.SysRole;
import com.zcurd.service.RoleService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleController extends BaseController {
	public void listPage() {
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
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_role", properties, symbols, values,
				orderBy, getPager());
		renderDatagrid(list,
				DBTool.countByMultPropertiesDbSource("zcurd_base", "sys_role", properties, symbols, values));
	}

	public void addPage() {
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		SysRole model = (SysRole) getModel(SysRole.class, "model");
		if (SysRole.me.findFirst("select id from sys_role where role_name = ?",
				new Object[] { model.getStr("role_name") }) != null) {
			addOpLog("[角色管理] 增加失败，[名称重复]");
			renderFailed("操作失败[名称重复]");
		} else {
			((SysRole) getModel(SysRole.class, "model")).save();
			addOpLog("[角色管理] 增加");
			renderSuccess();
		}
	}

	public void updatePage() {
		setAttr("model", SysRole.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		SysRole model = (SysRole) SysRole.me.findById(getPara("id"));
		String notify_type = getPara("model.notify_type");
		model.set("role_name", getPara("model.role_name"));
		model.set("notify_type", notify_type);
		model.update();
		addOpLog("[角色管理] 修改");
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
			((SysRole) (new SysRole()).set("id", id)).delete();

			b++;
		}
		addOpLog("[角色管理] 删除");
		renderSuccess();
	}

	public void detailPage() {
		SysRole model = (SysRole) SysRole.me.findById(getParaToInt("id"));
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
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_role", properties, symbols, values);
		List<String> headers = new ArrayList<String>();
		List<String> clomuns = new ArrayList<String>();
		headers.add("角色名称");
		clomuns.add("role_name");
		CsvRender csvRender = new CsvRender(headers, list);
		csvRender.clomuns(clomuns);
		csvRender.fileName("角色管理");
		addOpLog("[角色管理] 导出cvs");
		render(csvRender);
	}

	public void editAuthPage() {
		int roleId = getParaToInt("roleId").intValue();
		setAttr("roleId", Integer.valueOf(roleId));
		List<Record> btnIds = Db.find(
				"select b.menu_id, b.id from sys_role_btn a join sys_menu_btn b on a.btn_id=b.id where role_id=?",
				new Object[] { Integer.valueOf(roleId) });
		List<Record> dataruleIds = Db.find(
				"select b.menu_id, b.id from sys_role_datarule a join sys_menu_datarule b on a.datarule_id=b.id where role_id=?",
				new Object[] { Integer.valueOf(roleId) });
		setAttr("btnIds", btnIds);
		setAttr("dataruleIds", dataruleIds);
		render("editAuth.html");
	}

	@Before({ com.zcurd.common.interceptor.MenuAuthInterceptor.class })
	public void editAuth() {
		String menuIds = getPara("menuIds");
		String btnIds = getPara("btnIds");
		String dataruleIds = getPara("dataruleIds");
		int roleId = getParaToInt("roleId").intValue();
		RoleService roleService = (RoleService) Duang.duang(RoleService.class);
		roleService.saveAuth(menuIds, btnIds, dataruleIds, roleId);
		setSessionAttr("authLoad", Boolean.valueOf(true));
		addOpLog("[权限管理] 修改");
		renderSuccess();
	}

	public void getAllMenu() {
		int roleId = getParaToInt("roleId", Integer.valueOf(0)).intValue();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("menuIds",
				Db.find("select * from sys_role_menu where role_id=?", new Object[] { Integer.valueOf(roleId) }));
		result.put("menuList", Menu.me.findAll());
		renderJson(result);
	}

	public void genAuthBtnBatch() {
		int menuId = getParaToInt("menuId").intValue();
		List<MenuBtn> modelList = new ArrayList<MenuBtn>();
		modelList
				.add((MenuBtn) ((MenuBtn) ((MenuBtn) ((MenuBtn) (new MenuBtn()).set("menu_id", Integer.valueOf(menuId)))
						.set("btn_name", "增加")).set("class_name", "addBtn")).set("method_name", "add,addPage"));
		modelList
				.add((MenuBtn) ((MenuBtn) ((MenuBtn) ((MenuBtn) (new MenuBtn()).set("menu_id", Integer.valueOf(menuId)))
						.set("btn_name", "修改")).set("class_name", "updateBtn")).set("method_name",
								"update,updatePage"));
		modelList
				.add((MenuBtn) ((MenuBtn) ((MenuBtn) ((MenuBtn) (new MenuBtn()).set("menu_id", Integer.valueOf(menuId)))
						.set("btn_name", "删除")).set("class_name", "delBtn")).set("method_name", "delete"));
		modelList
				.add((MenuBtn) ((MenuBtn) ((MenuBtn) ((MenuBtn) (new MenuBtn()).set("menu_id", Integer.valueOf(menuId)))
						.set("btn_name", "导出")).set("class_name", "exportBtn")).set("method_name", "exportCsv"));
		modelList
				.add((MenuBtn) ((MenuBtn) ((MenuBtn) ((MenuBtn) (new MenuBtn()).set("menu_id", Integer.valueOf(menuId)))
						.set("btn_name", "导入")).set("class_name", "importBtn")).set("method_name", "importExcelPage"));
		Db.batchSave(modelList, 1000);
		renderSuccess();
	}
}
