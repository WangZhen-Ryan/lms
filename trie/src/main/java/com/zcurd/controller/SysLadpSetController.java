package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.model.SysDict;
import java.util.List;
import java.util.Map;

public class SysLadpSetController extends BaseController {
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
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_dict", properties, symbols, values,
				orderBy, getPager());
		Map<String, Object> dictDatadict_type = SysDict.me.getDictDatadict_type();
		for (Record record : list) {
			String fieldName = "dict_type";
			if (record.get(fieldName) != null && dictDatadict_type.get(record.get(fieldName).toString()) != null)
				record.set(fieldName, dictDatadict_type.get(record.get(fieldName).toString()));
		}
		renderDatagrid(list,
				DBTool.countByMultPropertiesDbSource("zcurd_base", "sys_dict", properties, symbols, values));
	}

	public void addPage() {
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		SysDict sysDict = (SysDict) getModel(SysDict.class, "model");
		List<SysDict> find = SysDict.me.find("select id from sys_dict where dict_type=? and dict_key=?",
				new Object[] { sysDict.getStr("dict_type"), sysDict.getStr("dict_key") });
		if (find != null && !find.isEmpty()) {
			addOpLog("[数据字典] 增加失败，重复的键名称！");
			renderFailed("操作失败，重复的键名称！");
		} else {
			sysDict.save();
			addOpLog("[数据字典] 增加");
			renderSuccess();
		}
	}

	public void updatePage() {
		setAttr("model", SysDict.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		SysDict model = (SysDict) SysDict.me.findById(getPara("id"));
		model.set("dict_value", getPara("model.dict_value"));
		model.update();
		addOpLog("[数据字典] 修改");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void delete() {
		Long[] ids = getParaValuesToLong("id[]");
		byte b;
		int i;
		Long[] arrayOfLong;
		for (i = (arrayOfLong = ids).length, b = 0; b < i;) {
			Long id = arrayOfLong[b];
			((SysDict) (new SysDict()).set("id", id)).delete();

			b++;
		}
		addOpLog("[数据字典] 删除");
		renderSuccess();
	}

	public void detailPage() {
		SysDict model = (SysDict) SysDict.me.findById(getParaToInt("id"));
		Map<String, Object> dictDatadict_type = SysDict.me.getDictDatadict_type();
		if (dictDatadict_type.get(model.get("dict_type").toString()) != null)
			model.set("dict_type", dictDatadict_type.get(model.get("dict_type").toString()));
		setAttr("model", model);
		render("detail.html");
	}
}
