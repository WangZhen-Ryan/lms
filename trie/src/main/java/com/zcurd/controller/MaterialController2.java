package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.render.JsonRender;
import com.jfinal.render.Render;
import com.jfinal.upload.UploadFile;
import com.zcurd.common.util.Check;
import com.zcurd.common.util.CommonUtils;
import com.zcurd.common.util.ToolDateTime;
import com.zcurd.excel.ExcelUtils;
import com.zcurd.excel.utils.Utils;
import com.zcurd.ext.mail.MSSendMail;
import com.zcurd.model.Material2;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class MaterialController2 extends BaseController {
	public void listPage() {
		setAttr("dictDataCategory", Material2.me.getDictDataCategory());
		setAttr("dictDataStatus", Material2.me.getDictDataStatus());
		render("list.html");
	}

	public void listData() {
		Date d = new Date();
		String select = "select a.* ";
		Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
		Map<String, Object> dictDataCategory = Material2.me.getDictDataCategory();
		Map<String, Object> dictDataLocation = Material2.me.getDictDataLocation();
		Map<String, Object> dictDataType = Material2.me.getDictDataType();
		Map<String, Object> dictDataCode = Material2.me.getDictDataCode();
		Map<String, Object> dictDataStatus = Material2.me.getDictDataStatus();
		for (Record record : paginate.getList()) {
			CommonUtils.setFieldValue("Category", dictDataCategory, record);
			CommonUtils.setFieldValue("Location", dictDataLocation, record);
			CommonUtils.setFieldValue("Type", dictDataType, record);
			CommonUtils.setFieldValue("Code", dictDataCode, record);
			CommonUtils.setFieldValue("Status", dictDataStatus, record);
		}
		System.out.println(String.valueOf((new Date()).getTime() - d.getTime()) + "-----");
		renderDatagrid(paginate);
	}

	private String sqlExceptSelect() {
		String sqlSelect = getSearchText("material2");
		String sqlExceptSelect = " from material2 a,sys_address_dict b where a.location  = b.id ";
		String location = getPara("likeloc");
		String project = getPara("Project");
		String code = getPara("Code");
		String type = getPara("Type");
		String category = getPara("Category");
		String status = getPara("Status");
		String user = getPara("user");
		String appointment_name = getPara("appointment_name");
		if (StringUtils.isNotEmpty(project))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.project like '%" + project + "%'";
		if (StringUtils.isNotEmpty(code))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.code = '" + code + "'";
		if (StringUtils.isNotEmpty(type))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.type = '" + type + "'";
		if (StringUtils.isNotEmpty(category))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.category = '" + category + "'";
		if (StringUtils.isNotEmpty(location))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'";
		if (StringUtils.isNoneEmpty(new CharSequence[] { appointment_name }))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.appointment_name ='" + appointment_name + "'";
		if (StringUtils.isNoneEmpty(new CharSequence[] { status }))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.status ='" + status + "'";
		if (StringUtils.isNotEmpty(user))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.user like '%" + user + "%'";
		if (StringUtils.isNotEmpty(sqlSelect))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and (" + sqlSelect + ")";
		String orderby = "";
		String sort = getPara("sort");
		String order = getPara("order");
		if (!Check.IsStringNULL(sort) && !Check.IsStringNULL(order)) {
			List<String> sorts = Arrays.asList(sort.split(","));
			List<String> orders = Arrays.asList(order.split(","));
			if (sorts.size() > 0 && orders.size() > 0 && sorts.size() == sorts.size())
				for (int i = 0; i < sorts.size(); i++)
					orderby = String.valueOf(orderby) + (String) sorts.get(i) + " " + (String) orders.get(i) + ",";
		}
		return String.valueOf(sqlExceptSelect) + "order by " + orderby + " a.id desc";
	}

	public void addPage() {
		setAttr("dictDataCategory", Material2.me.getDictDataCategory());
		setAttr("dictDataLocation", Material2.me.getDictDataLocation());
		setAttr("dictDataType", Material2.me.getDictDataType());
		setAttr("dictDataCode", Material2.me.getDictDataCode());
		setAttr("dictDataStatus", Material2.me.getDictDataStatus());
		setAttr("dictDataUser", Material2.me.getDictDataUser());
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		Material2 model = (Material2) getModel(Material2.class, "model");
		model.set("Handler", getSessionUser().getStr("display_name"));
		Map<String, Object> dictDataUser = Material2.me.getDictDataUser();
		CommonUtils.setFieldValue("User", "User_id", dictDataUser, model);
		CommonUtils.setFieldValue("Project", "Project_id", dictDataUser, model);
		model.save();
		addOpLog("[物料2] 增加");
		renderSuccess();
	}

	public void picpage() {
		Material2 model = (Material2) Material2.me.findById(getPara("id"));
		Map<String, Object> dictDataCategory = Material2.me.getDictDataCategory();
		Map<String, Object> dictDataLocation = Material2.me.getDictDataLocation();
		Map<String, Object> dictDataType = Material2.me.getDictDataType();
		Map<String, Object> dictDataCode = Material2.me.getDictDataCode();
		Map<String, Object> dictDataStatus = Material2.me.getDictDataStatus();
		CommonUtils.setFieldValue("Category", dictDataCategory, model);
		CommonUtils.setFieldValue("Location", dictDataLocation, model);
		CommonUtils.setFieldValue("Type", dictDataType, model);
		CommonUtils.setFieldValue("Code", dictDataCode, model);
		CommonUtils.setFieldValue("Status", dictDataStatus, model);
		setAttr("model", model);
		render("pic.html");
	}

	public void stockPage() {
		Material2 model = (Material2) Material2.me.findById(getPara("id"));
		Map<String, Object> dictDataCategory = Material2.me.getDictDataCategory();
		Map<String, Object> dictDataLocation = Material2.me.getDictDataLocation();
		Map<String, Object> dictDataType = Material2.me.getDictDataType();
		Map<String, Object> dictDataCode = Material2.me.getDictDataCode();
		Map<String, Object> dictDataStatus = Material2.me.getDictDataStatus();
		CommonUtils.setFieldValue("Category", dictDataCategory, model);
		CommonUtils.setFieldValue("Location", dictDataLocation, model);
		CommonUtils.setFieldValue("Type", dictDataType, model);
		CommonUtils.setFieldValue("Code", dictDataCode, model);
		CommonUtils.setFieldValue("Status", dictDataStatus, model);
		setAttr("dictDataUser", Material2.me.getDictDataUser());
		setAttr("model", model);
		render("stock.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void stock() {
		Material2 model = (Material2) Material2.me.findById(getPara("id"));
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "4")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("material_log", record);
		model.set("stock_exist", getPara("model.stock_exist"));
		model.set("stock_time", getPara("model.stock_time"));
		model.set("stock_man", getPara("model.stock_man"));
		model.set("stock_remark", getPara("model.stock_remark"));
		Map<String, Object> dictDataUser = Material2.me.getDictDataUser();
		CommonUtils.setFieldValue("stock_man", dictDataUser, model);
		model.update();
		addOpLog("[物料2] 盘点");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDataCategory", Material2.me.getDictDataCategory());
		setAttr("dictDataLocation", Material2.me.getDictDataLocation());
		setAttr("dictDataType", Material2.me.getDictDataType());
		setAttr("dictDataCode", Material2.me.getDictDataCode());
		setAttr("dictDataStatus", Material2.me.getDictDataStatus());
		setAttr("dictDataUser", Material2.me.getDictDataUser());
		Material2 model = (Material2) Material2.me.findById(getPara("id"));
		if (!Check.IsStringNULL((String) model.get("User_id")))
			model.set("User", model.get("User_id"));
		if (!Check.IsStringNULL((String) model.get("Project_id")))
			model.set("Project", model.get("Project_id"));
		setAttr("model", model);
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		Material2 model = (Material2) Material2.me.findById(getPara("id"));
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "0")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("material_log", record);
		model.set("Category", getPara("model.Category"));
		model.set("Location", getPara("model.Location"));
		model.set("Type", getPara("model.Type"));
		model.set("Code", getPara("model.Code"));
		model.set("ICS", getPara("model.ICS"));
		model.set("SN", getPara("model.SN"));
		model.set("Status", getPara("model.Status"));
		model.set("User", getPara("model.User"));
		model.set("OUT_from", getPara("model.OUT_from"));
		model.set("OUT_till", getPara("model.OUT_till"));
		model.set("Project", getPara("model.Project"));
		model.set("Platform", getPara("model.Platform"));
		model.set("Remarks", getPara("model.Remarks"));
		model.set("Inventory_Date", getPara("model.Inventory_Date"));
		model.set("PR_EMD_No", getPara("model.PR_EMD_No"));
		model.set("PO_UPE", getPara("model.PO_UPE"));
		model.set("Handler", getSessionUser().getStr("display_name"));
		Map<String, Object> dictDataUser = Material2.me.getDictDataUser();
		CommonUtils.setFieldValue("User", "User_id", dictDataUser, model);
		CommonUtils.setFieldValue("Project", "Project_id", dictDataUser, model);
		model.update();
		addOpLog("[物料2] 修改");
		renderSuccess();
	}

	public void lendPage() {
		setAttr("idlist", getPara("idlist"));
		setAttr("strProejct", getSessionUser().getStr("display_name"));
		setAttr("dictDataUser", Material2.me.getDictDataUser());
		render("lend.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void lend() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Map<String, Object> dictDataUser = Material2.me.getDictDataUser();
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Material2 model = (Material2) Material2.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "2")
					.set("update_time", timestamp).remove("id");
			Db.save("material_log", record);
			model.set("User", getPara("model.User"));
			model.set("Project", getPara("model.Project"));
			model.set("OUT_from", getPara("model.OUT_from"));
			model.set("OUT_till", getPara("model.OUT_till"));
			CommonUtils.setFieldValue("User", "User_id", dictDataUser, model);
			CommonUtils.setFieldValue("Project", "Project_id", dictDataUser, model);
			model.set("Status", "out");
			model.update();
		}
		addOpLog("[物料2] 借出");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void back() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Material2 model = (Material2) Material2.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "3")
					.set("update_time", timestamp).remove("id");
			Db.save("material_log", record);
		}
		Db.update(
				"UPDATE MATERIAL2 SET OUT_till = NULL, USER = null,USER_ID = null, OUT_from = NULL,Status='free' WHERE id IN (?)",
				new Object[] { getPara("idlist") });
		addOpLog("[物料2] 归还");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void delete() {
		Long[] ids = getParaValuesToLong("id[]");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		byte b;
		int i;
		Long[] arrayOfLong;
		for (i = (arrayOfLong = ids).length, b = 0; b < i;) {
			Long id = arrayOfLong[b];
			Material2 model = (Material2) Material2.me.findById(id);
			((Material2) (new Material2()).set("id", id)).delete();

			Record record = new Record();
			record.setColumns((Model) model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "1")
					.set("update_time", timestamp).remove("id");
			Db.save("material_log", record);
			b++;
		}
		addOpLog("[material2] 删除");
		renderSuccess();
	}

	private static Map<String, Object> staticMap = new HashMap<String, Object>() {
		private static final long serialVersionUID = 1L;
	};

	private HSSFWorkbook wb;

	public void detailPage() {
		Material2 model = (Material2) Material2.me.findById(getParaToLong("id"));
		Map<String, Object> dictDataCategory = Material2.me.getDictDataCategory();
		CommonUtils.setFieldValue("Category", dictDataCategory, model);
		Map<String, Object> dictDataLocation = Material2.me.getDictDataLocation();
		CommonUtils.setFieldValue("Location", dictDataLocation, model);
		Map<String, Object> dictDataType = Material2.me.getDictDataType();
		CommonUtils.setFieldValue("Type", dictDataType, model);
		Map<String, Object> dictDataCode = Material2.me.getDictDataCode();
		CommonUtils.setFieldValue("Code", dictDataCode, model);
		Map<String, Object> dictDataStatus = Material2.me.getDictDataStatus();
		CommonUtils.setFieldValue("Status", dictDataStatus, model);
		CommonUtils.setFieldValue("stock_exist", staticMap, model);
		setAttr("model", model);
		render("detail.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importpic() {
		String id = getPara("id");
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传物料照片...");
		Map<String, Object> result = new HashMap<String, Object>();
		String baPath = String.valueOf(PathKit.getWebRootPath()) + File.separator + "res" + File.separator + "upload"
				+ File.separator + "pic" + File.separator + "material2";
		File target = new File(baPath);
		if (!target.exists())
			target.mkdirs();
		UploadFile file = getFile();
		File uploadFile = file.getFile();
		String fileName = uploadFile.getName();
		String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
		target = new File(String.valueOf(baPath) + File.separator + id + "." + suffix);
		if (target.exists() && target.isFile())
			target.delete();
		uploadFile.renameTo(target);
		uploadFile.delete();
		Material2 model = (Material2) Material2.me.findById(id);
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "5")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("material_log", record);
		model.set("pic", String.valueOf(id) + "." + suffix);
		model.update();
		addOpLog("[material2]上传成功!");
		result.put("result", "success");
		result.put("msg", "上传物料照片成功!");
		render((new JsonRender(result)).forIE());
	}

	public void appointmentPage() {
		setAttr("idlist", getPara("idlist"));
		render("appointment.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void appointment() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Record record = null;
		String mailText = "注意您有新的物料预约申请！<br>";
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Material2 model = (Material2) Material2.me.findById(id);
			record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "7")
					.set("update_time", timestamp).remove("id");
			Db.save("material_log", record);
			model.set("appointment_id", getSessionUser().getStr("id"));
			model.set("appointment_name", getSessionUser().getStr("display_name"));
			model.set("appointment_start", getPara("model.appointment_start"));
			model.set("appointment_end", getPara("model.appointment_end"));
			model.set("appointment_remark", getPara("model.appointment_remark"));
			model.set("appointment_save_time", timestamp);
			model.update();
			String locationValue = Material2.me.getDictDataLocation(model.get("Location").toString());
			mailText = String.valueOf(mailText) + "Category分类名册:" + model.get("Category") + "; Location位置:"
					+ locationValue + "; Type名称:" + model.get("Type") + "; Code产品编码:" + model.get("Code") + " <br>";
		}
		mailText = String.valueOf(mailText) + "<br>";
		mailText = String.valueOf(mailText) + "预约日期起:" + getPara("model.appointment_start") + "<br>";
		mailText = String.valueOf(mailText) + "预约日期止:" + getPara("model.appointment_end") + "<br>";
		mailText = String.valueOf(mailText) + "预约备注:" + getPara("model.appointment_remark") + "<br>";
		mailText = String.valueOf(mailText) + "预约人:" + getSessionUser().getStr("display_name") + "<br>";
		MSSendMail.xx();
		addOpLog("[物料管理2] 预约");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void clearAppointment() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Material2 model = (Material2) Material2.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "6")
					.set("update_time", timestamp).remove("id");
			Db.save("material_log", record);
		}
		String sql = "UPDATE material2 SET appointment_id = NULL, appointment_name = null, appointment_start=null,appointment_end = NULL, appointment_remark = null,appointment_save_time=null WHERE id IN ("
				+ getPara("idlist") + ")";
		Db.update(sql);
		addOpLog("[物料2] 预约记录清除");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传物料信息文件...");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		result.put("msg", "操作失败！");
		UploadFile file = getFile();
	    File uploadFile = file.getFile();
	    FileInputStream fis = null;
	    FileInputStream fis1 = null;
	    BufferedInputStream bis = null;
	    BufferedInputStream bis1 = null;
		try {
			fis = new FileInputStream(uploadFile);
			bis = new BufferedInputStream(fis);
			List<List<String>> ruleExcelListList = ExcelUtils.getInstance().readExcel2List(bis, 0, "valiRule");
			Map<Integer, String[]> ruleMap = new HashMap<Integer, String[]>();
			if (ruleExcelListList == null || ruleExcelListList.isEmpty()) {
				result.put("result", "fail");
				result.put("msg", "对不起，文件不符合模板。");
			} else {
				for (int i = 0; i < ruleExcelListList.size(); i++)
					ruleMap.put(Integer.valueOf(i),
							(String[]) ((List) ruleExcelListList.get(i)).toArray(new String[0]));
				fis1 = new FileInputStream(uploadFile);
				bis1 = new BufferedInputStream(fis1);
				List<List<String>> readExcel2List = ExcelUtils.getInstance().readExcel2List(bis1, 1);
				if (readExcel2List == null || readExcel2List.isEmpty()) {
					result.put("result", "fail");
					result.put("msg", "对不起，请向文件中写入内容！");
				} else {
					List<Record> categoryList = Db.find(getDictSql("category"));
					List<Record> find = Db
							.find("select id as k ,auto_val as v from sys_address_dict  where `status`='1'");
					List<Record> find3 = Db.find(getDictSql("状态"));
					List<Record> find4 = Db.find(getDictSql("material_name"));
					List<Record> find5 = Db.find(getDictSql("produce_code"));
					if (find == null || find.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护地址字典菜单中地址编码信息！");
					} else if (find3 == null || find3.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护数据字典的状态信息！");
					} else if (find4 == null || find4.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护数据字典的物料名称信息！");
					} else if (find5 == null || find5.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护数据字典的产品编码信息！");
					} else if (categoryList == null || categoryList.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护数据字典的分类名册信息！");
					} else {
						Map<String, Long> rackAddressMap = new HashMap<String, Long>();
						for (int i = 0; i < find.size(); i++) {
							Record record = (Record) find.get(i);
							rackAddressMap.put(record.getStr("v"), record.getLong("k"));
						}
						Map<String, String> statusMap = CommonUtils.recordListToMap(find3);
						Map<String, String> materialNameMap = CommonUtils.recordListToMap(find4);
						Map<String, String> produceCodeMap = CommonUtils.recordListToMap(find5);
						Map<String, String> categoryMap = CommonUtils.recordListToMap(categoryList);
						List<String> listResult = new ArrayList<String>();
						List<Record> batchRecord = new ArrayList<Record>();
						Record record = null;
						boolean isUseRecord = true;
						for (int i = 0; i < readExcel2List.size(); i++) {
							List<String> list = (List) readExcel2List.get(i);
							record = new Record();
							for (int j = 0; j < list.size(); j++) {
								boolean valiflag = true;
								String val = (String) list.get(j);
								String[] strings = (String[]) ruleMap.get(Integer.valueOf(j));
								if ("required:true".equals(strings[3]) && StringUtils.isEmpty(val)) {
									valiflag = false;
									listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据为必录项");
								}
								if (valiflag && StringUtils.isNotEmpty(val)) {
									Object v = null;
									if (strings[2].startsWith("Long")) {
										try {
											v = Utils.str2TargetClass(val, Long.class);
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据应为数字");
										}
									} else if (strings[2].startsWith("Date")) {
										try {
											v = Utils.str2TargetClass(val, Date.class);
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据应为日期");
										}
									} else if (strings[2].startsWith("Integer")) {
										try {
											v = Utils.str2TargetClass(val, Integer.class);
											String[] split = strings[2].split(":");
											if (split.length > 1) {
												valiflag = (((Integer) v).intValue() >= Integer.parseInt(split[1]));
												if (!valiflag) {
													listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列最小值为"
															+ Integer.parseInt(split[1]));
												} else if (split.length == 3) {
													valiflag = (((Integer) v).intValue() <= Integer.parseInt(split[2]));
													if (!valiflag)
														listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列最大值为"
																+ Integer.parseInt(split[2]));
												}
											}
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据应为数字");
										}
									} else {
										v = val;
									}
									if (valiflag) {
										switch (j) {
										case 0:
											if (categoryMap.containsKey(v)) {
												v = categoryMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于分类名册字典中");
											break;
										case 1:
											if (rackAddressMap.containsKey(v)) {
												v = rackAddressMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于位置字典中");
											break;
										case 2:
											if (materialNameMap.containsKey(v)) {
												v = materialNameMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于物料名称字典中");
											break;
										case 3:
											if (produceCodeMap.containsKey(v)) {
												v = produceCodeMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于产品编码字典中");
											break;
										case 6:
											if (statusMap.containsKey(v)) {
												v = statusMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于状态字典中");
											break;
										}
										record.set(strings[1], v);
									}
								}
								isUseRecord = isUseRecord ? valiflag : isUseRecord;
							}
							if (isUseRecord)
								batchRecord.add(record);
						}
						if (listResult.isEmpty()) {
							for (int i = 0; i < batchRecord.size(); i++)
								Db.save("material2", (Record) batchRecord.get(i));
							result.put("result", "success");
							result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传物料信息文件结束，共入库"
									+ batchRecord.size() + "条数据");
						} else {
							result.put("result", "fail");
							result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传物料信息文件异常结束");
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("上传文件未找到", e);
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error("读取Excel文件或入库异常,请先下载模板后再录入", e);
			result.put("result", "fail");
			result.put("msg", "读取Excel文件或入库异常，请先下载模板后再录入！[" + e.getMessage() + "]");
		} finally {
			IOUtils.closeQuietly(new Closeable[] { fis, bis, fis1, bis1 });
			uploadFile.delete();
		}
		render((new JsonRender(result)).forIE());
	}

	public void expExcel() {
		String select = "select a.* ";
		List<Record> paginate2 = Db.find(String.valueOf(select) + sqlExceptSelect());
		Map<String, Object> dictDataCategory = Material2.me.getDictDataCategory();
		Map<String, Object> dictDataLocation = Material2.me.getDictDataLocation();
		Map<String, Object> dictDataType = Material2.me.getDictDataType();
		Map<String, Object> dictDataCode = Material2.me.getDictDataCode();
		Map<String, Object> dictDataStatus = Material2.me.getDictDataStatus();
		for (Record record : paginate2) {
			CommonUtils.setFieldValue("Category", dictDataCategory, record);
			CommonUtils.setFieldValue("Location", dictDataLocation, record);
			CommonUtils.setFieldValue("Type", dictDataType, record);
			CommonUtils.setFieldValue("Code", dictDataCode, record);
			CommonUtils.setFieldValue("Status", dictDataStatus, record);
		}
		String fileName = "物料表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
		String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
		System.out.println(path);
		File file = new File(path);
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("物料表");
		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("分类名册");
		row0.createCell(1).setCellValue("位置");
		row0.createCell(2).setCellValue("名称");
		row0.createCell(3).setCellValue("产品编码");
		row0.createCell(4).setCellValue("版本");
		row0.createCell(5).setCellValue("序列号");
		row0.createCell(6).setCellValue("状态");
		row0.createCell(7).setCellValue("使用人");
		row0.createCell(8).setCellValue("借用日");
		row0.createCell(9).setCellValue("归还日");
		row0.createCell(10).setCellValue("申请人");
		row0.createCell(11).setCellValue("最后修改人");
		row0.createCell(12).setCellValue("平台");
		row0.createCell(13).setCellValue("备注");
		row0.createCell(14).setCellValue("录入日期");
		row0.createCell(15).setCellValue("对应PR_EMD");
		row0.createCell(16).setCellValue("对应PO工程号");
		row0.createCell(17).setCellValue("是否存在");
		row0.createCell(18).setCellValue("盘点人");
		row0.createCell(19).setCellValue("盘点时间");
		row0.createCell(20).setCellValue("盘点注释");
		row0.createCell(21).setCellValue("预约人姓名");
		row0.createCell(22).setCellValue("预约日期起");
		row0.createCell(23).setCellValue("预约日期止");
		row0.createCell(24).setCellValue("预约备注");
		for (int i = 0; i < paginate2.size(); i++) {
			Record record2 = (Record) paginate2.get(i);
			HSSFRow rowi = sheet.createRow(i + 1);
			rowi.createCell(0)
					.setCellValue((record2.get("Category") == null) ? "" : record2.get("Category").toString());
			rowi.createCell(1)
					.setCellValue((record2.get("Location") == null) ? "" : record2.get("Location").toString());
			rowi.createCell(2).setCellValue((record2.get("Type") == null) ? "" : record2.get("Type").toString());
			rowi.createCell(3).setCellValue((record2.get("Code") == null) ? "" : record2.get("Code").toString());
			rowi.createCell(4).setCellValue((record2.get("ICS") == null) ? "" : record2.get("ICS").toString());
			rowi.createCell(5).setCellValue((record2.get("SN") == null) ? "" : record2.get("SN").toString());
			rowi.createCell(6).setCellValue((record2.get("Status") == null) ? "" : record2.get("Status").toString());
			rowi.createCell(7).setCellValue((record2.get("User") == null) ? "" : record2.get("User").toString());
			rowi.createCell(8)
					.setCellValue((record2.get("OUT_from") == null) ? "" : record2.get("OUT_from").toString());
			rowi.createCell(9)
					.setCellValue((record2.get("OUT_till") == null) ? "" : record2.get("OUT_till").toString());
			rowi.createCell(10).setCellValue((record2.get("Project") == null) ? "" : record2.get("Project").toString());
			rowi.createCell(11).setCellValue((record2.get("Handler") == null) ? "" : record2.get("Handler").toString());
			rowi.createCell(12)
					.setCellValue((record2.get("Platform") == null) ? "" : record2.get("Platform").toString());
			rowi.createCell(13).setCellValue((record2.get("Remarks") == null) ? "" : record2.get("Remarks").toString());
			rowi.createCell(14).setCellValue(
					(record2.get("Inventory_Date") == null) ? "" : record2.get("Inventory_Date").toString());
			rowi.createCell(15)
					.setCellValue((record2.get("PR_EMD_No") == null) ? "" : record2.get("PR_EMD_No").toString());
			rowi.createCell(16).setCellValue((record2.get("PO_UPE") == null) ? "" : record2.get("PO_UPE").toString());
			rowi.createCell(17).setCellValue(escapeCode((String) record2.get("stock_exist")));
			rowi.createCell(18)
					.setCellValue((record2.get("stock_man") == null) ? "" : record2.get("stock_man").toString());
			rowi.createCell(19)
					.setCellValue((record2.get("stock_time") == null) ? ""
							: ((record2.get("stock_time").toString().length() > 10)
									? record2.get("stock_time").toString().substring(0, 10)
									: record2.get("stock_time").toString()));
			rowi.createCell(20)
					.setCellValue((record2.get("stock_remark") == null) ? "" : record2.get("stock_remark").toString());
			rowi.createCell(21).setCellValue(
					(record2.get("appointment_name") == null) ? "" : record2.get("appointment_name").toString());
			rowi.createCell(22).setCellValue(appointmentTime(record2.get("appointment_start")));
			rowi.createCell(23).setCellValue(appointmentTime(record2.get("appointment_end")));
			rowi.createCell(24).setCellValue(
					(record2.get("appointment_remark") == null) ? "" : record2.get("appointment_remark").toString());
		}
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(file);
			this.wb.write(fileOut);
			renderFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			renderSuccess("导出失败!");
		} finally {
			if (fileOut != null)
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
