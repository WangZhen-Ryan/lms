package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.DBTool;
import com.zcurd.common.ZcurdTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.ext.render.csv.CsvRender;
import com.zcurd.model.TaskBase;
import com.zcurd.service.TaskService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
public class TaskBaseController extends BaseController {
	public void listPage() {
		setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
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
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "task_base", properties, symbols, values,
				orderBy, getPager());
		ZcurdTool.replaceDict(TaskBase.me.getDictDatatarget_type(), list, "target_type");
		ZcurdTool.replaceDict(TaskBase.me.getDictDatastatus(), list, "status");
		renderDatagrid(list,
				DBTool.countByMultPropertiesDbSource("zcurd_base", "task_base", properties, symbols, values));
	}

	public void addPage() {
		setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		render("add.html");
	}

	public void add() {
		TaskBase model = (TaskBase) getModel(TaskBase.class, "model");
		TaskService taskService = (TaskService) Duang.duang(TaskService.class);
		taskService.add(model);
		addOpLog("[定时任务] 增加");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		setAttr("model", TaskBase.me.findById(getPara("id")));
		render("update.html");
	}

	public void update() {
		TaskBase model = (TaskBase) TaskBase.me.findById(getPara("id"));
		model.setName(getPara("model.name"));
		model.setTargetType(getParaToInt("model.target_type"));
		model.setTargetValue(getPara("model.target_value"));
		model.setCron(getPara("model.cron"));
		model.setStatus(getParaToInt("model.status"));
		TaskService taskService = (TaskService) Duang.duang(TaskService.class);
		taskService.update(model);
		addOpLog("[定时任务] 修改");
		renderSuccess();
	}

	public void delete() {
		Integer[] ids = getParaValuesToInt("id[]");
		TaskService taskService = (TaskService) Duang.duang(TaskService.class);
		byte b;
		int i;
		Integer[] arrayOfInteger;
		for (i = (arrayOfInteger = ids).length, b = 0; b < i;) {
			Integer id = arrayOfInteger[b];
			taskService.delete(id);
			b++;
		}
		addOpLog("[定时任务] 删除");
		renderSuccess();
	}

	public void detailPage() {
		TaskBase model = (TaskBase) TaskBase.me.findById(getParaToInt("id"));
		Map<String, Object> dictDatatarget_type = TaskBase.me.getDictDatatarget_type();
		if (dictDatatarget_type.get(model.get("target_type").toString()) != null)
			model.set("target_type", dictDatatarget_type.get(model.get("target_type").toString()));
		Map<String, Object> dictDatastatus = TaskBase.me.getDictDatastatus();
		if (dictDatastatus.get(model.get("status").toString()) != null)
			model.set("status", dictDatastatus.get(model.get("status").toString()));
		setAttr("model", model);
		render("detail.html");
	}

	public void startOrStop() {
		TaskService taskService = (TaskService) Duang.duang(TaskService.class);
		taskService.startOrStop(getParaToInt("id").intValue(), getParaToInt("status").intValue());
		renderSuccess();
	}

	public void runAtSoon() {
		TaskService taskService = (TaskService) Duang.duang(TaskService.class);
		TaskBase taskBase = (TaskBase) TaskBase.me.findById(getPara("id"));
		taskService.runAtSoon(taskBase);
		renderSuccess();
	}

	public void exportCsv() {
		Object[] queryParams = getQueryParams();
		String[] properties = (String[]) queryParams[0];
		String[] symbols = (String[]) queryParams[1];
		Object[] values = (Object[]) queryParams[2];
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy))
			orderBy = "id desc";
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "task_base", properties, symbols, values);
		List<String> headers = new ArrayList<String>();
		List<String> clomuns = new ArrayList<String>();
		headers.add("名称");
		clomuns.add("name");
		headers.add("目标类型");
		clomuns.add("target_type");
		headers.add("目标值");
		clomuns.add("target_value");
		headers.add("cron表达式");
		clomuns.add("cron");
		headers.add("上次执行时间");
		clomuns.add("last_run_time");
		headers.add("上次执行耗时");
		clomuns.add("last_run_time_cost");
		headers.add("状态");
		clomuns.add("status");
		CsvRender csvRender = new CsvRender(headers, list);
		csvRender.clomuns(clomuns);
		csvRender.fileName("定时任务");
		addOpLog("[定时任务] 导出cvs");
		render(csvRender);
	}
}
