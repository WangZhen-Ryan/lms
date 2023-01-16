package com.zcurd.controller;

import com.zcurd.model.Menu;

public class MenuController extends BaseController {
	public void listAll() {
		renderJson(Menu.me.findAll());
	}

	public void list() {
		render("list.html");
	}

	public void addPage() {
		render("add.html");
	}

	public void add() {
		if (((Menu) getModel(Menu.class, "model")).save()) {
			addOpLog("[菜单管理] 增加");
			renderSuccess();
		} else {
			renderFailed();
		}
	}

	public void updatePage() {
		setAttr("model", Menu.me.findById(getParaToInt("id")));
		render("update.html");
	}

	public void update() {
		if (((Menu) getModel(Menu.class, "model")).update()) {
			addOpLog("[菜单管理] 修改");
			renderSuccess();
		} else {
			renderFailed();
		}
	}

	public void delete() {
		Integer[] ids = getParaValuesToInt("id[]");
		byte b;
		int i;
		Integer[] arrayOfInteger;
		for (i = (arrayOfInteger = ids).length, b = 0; b < i;) {
			Integer id = arrayOfInteger[b];
			Menu.me.deleteById(id);
			b++;
		}
		addOpLog("[菜单管理] 删除");
		renderSuccess();
	}
}
