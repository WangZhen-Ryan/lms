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
import com.zcurd.common.util.MailUtil;
import com.zcurd.common.util.ToolDateTime;
import com.zcurd.excel.ExcelUtils;
import com.zcurd.excel.utils.Utils;
import com.zcurd.model.Instrument;
import com.zcurd.model.Material;
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

public class InstrumentController extends BaseController {
	private HSSFWorkbook wb;

	public void listPage() {
		setAttr("dictDatainstrumenttype", Instrument.me.getDictDatainstrumenttype());
		setAttr("dictDataStatus", Material.me.getDictDataStatus());
		render("list.html");
	}

	public void listData() {
		String select = "select a.* ";
		Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
		Map<String, Object> dictDatalocation = Instrument.me.getDictDatalocation();
		Map<String, Object> dictDatastatus = Instrument.me.getDictDatastatus();
		Map<String, Object> dictDatamodulemo = Instrument.me.getDictDatamodulemo();
		Map<String, Object> dictDatamoduletype = Instrument.me.getDictDatamoduletype();
		Map<String, Object> dictDatainstrumenttype = Instrument.me.getDictDatainstrumenttype();
		for (Record record : paginate.getList()) {
			CommonUtils.setFieldValue("location", dictDatalocation, record);
			CommonUtils.setFieldValue("status", dictDatastatus, record);
			CommonUtils.setFieldValue("modulemo", dictDatamodulemo, record);
			CommonUtils.setFieldValue("moduletype", dictDatamoduletype, record);
			CommonUtils.setFieldValue("instrument_type", dictDatainstrumenttype, record);
		}
		renderDatagrid(paginate);
	}

	private String sqlExceptSelect() {
		String sqlSelect = getSearchText("instrument");
		String sqlExceptSelect = " from instrument a,sys_address_dict b where a.location  = b.id ";
		String location = getPara("Location");
		String project = getPara("Project");
		String instrument_type = getPara("instrument_type");
		String appointment_name = getPara("appointment_name");
		String status = getPara("status");
		String borrower = getPara("borrower");
		String user = getPara("user");
		if (StringUtils.isNotEmpty(project))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.project like '%" + project + "%'";
		if (StringUtils.isNotEmpty(location))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'";
		if (StringUtils.isNotEmpty(instrument_type))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.instrument_type ='" + instrument_type + "'";
		if (StringUtils.isNoneEmpty(new CharSequence[] { appointment_name }))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.appointment_name ='" + appointment_name + "'";
		if (StringUtils.isNotEmpty(borrower))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.borrower like '%" + borrower + "%'";
		if (StringUtils.isNotEmpty(sqlSelect))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and (" + sqlSelect + ")";
		if (StringUtils.isNoneEmpty(new CharSequence[] { status }))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.status ='" + status + "'";
		if (StringUtils.isNotEmpty(user))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.user like '%" + user + "%'";
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
		setAttr("dictDatalocation", Instrument.me.getDictDatalocation());
		setAttr("dictDatastatus", Instrument.me.getDictDatastatus());
		setAttr("dictDatamodulemo", Instrument.me.getDictDatamodulemo());
		setAttr("dictDatamoduletype", Instrument.me.getDictDatamoduletype());
		setAttr("dictDatainstrumenttype", Instrument.me.getDictDatainstrumenttype());
		setAttr("dictDataUser", Material.me.getDictDataUser());
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		Instrument model = (Instrument) getModel(Instrument.class, "model");
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("borrower", "borrower_id", dictDataUser, model);
		CommonUtils.setFieldValue("user", "borrower_id", dictDataUser, model);
		model.save();
		addOpLog("[????????????] ??????");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDatalocation", Instrument.me.getDictDatalocation());
		setAttr("dictDatastatus", Instrument.me.getDictDatastatus());
		setAttr("dictDatamodulemo", Instrument.me.getDictDatamodulemo());
		setAttr("dictDatamoduletype", Instrument.me.getDictDatamoduletype());
		setAttr("dictDatainstrumenttype", Instrument.me.getDictDatainstrumenttype());
		setAttr("dictDataUser", Material.me.getDictDataUser());
		Instrument model = (Instrument) Instrument.me.findById(getPara("id"));
		if (!Check.IsStringNULL((String) model.get("user_id")))
			model.set("user", model.get("user_id"));
		if (!Check.IsStringNULL((String) model.get("borrower_id")))
			model.set("borrower", model.get("borrower_id"));
		setAttr("model", model);
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		Instrument model = (Instrument) Instrument.me.findById(getPara("id"));
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "0")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("instrument_log", record);
		model.set("instrument_type", getPara("model.instrument_type"));
		model.set("location", getPara("model.location"));
		model.set("date", getPara("model.date"));
		model.set("mdf", getPara("model.mdf"));
		model.set("odf", getPara("model.odf"));
		model.set("fiber", getPara("model.fiber"));
		model.set("ip", getPara("model.ip"));
		model.set("modulemo", getPara("model.modulemo"));
		model.set("moduletype", getPara("model.moduletype"));
		model.set("port", getPara("model.port"));
		model.set("meterno", getPara("model.meterno"));
		model.set("number", getPara("model.number"));
		model.set("status", getPara("model.status"));
		model.set("assetno", getPara("model.assetno"));
		model.set("modulesn", getPara("model.modulesn"));
		model.set("sn", getPara("model.sn"));
		model.set("owner", getPara("model.owner"));
		model.set("remark", getPara("model.remark"));
		model.set("borrower", getPara("model.borrower"));
		model.set("user", getPara("model.user"));
		model.set("outfrom", getPara("model.outfrom"));
		model.set("outtill", getPara("model.outtill"));
		model.set("project", getPara("model.project"));
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("borrower", "borrower_id", dictDataUser, model);
		CommonUtils.setFieldValue("user", "user_id", dictDataUser, model);
		model.update();
		addOpLog("[????????????] ??????");
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
			Instrument model = (Instrument) Instrument.me.findById(id);
			((Instrument) (new Instrument()).set("id", id)).delete();

			Record record = new Record();
			record.setColumns((Model) model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "1")
					.set("update_time", timestamp).remove("id");
			Db.save("instrument_log", record);
			b++;
		}
		addOpLog("[????????????] ??????");
		renderSuccess();
	}

	public void detailPage() {
		Instrument model = (Instrument) Instrument.me.findById(getPara("id"));
		Map<String, Object> dictDatalocation = Instrument.me.getDictDatalocation();
		Map<String, Object> dictDatastatus = Instrument.me.getDictDatastatus();
		Map<String, Object> dictDatamodulemo = Instrument.me.getDictDatamodulemo();
		Map<String, Object> dictDatamoduletype = Instrument.me.getDictDatamoduletype();
		Map<String, Object> dictDatainstrumenttype = Instrument.me.getDictDatainstrumenttype();
		CommonUtils.setFieldValue("location", dictDatalocation, model);
		CommonUtils.setFieldValue("status", dictDatastatus, model);
		CommonUtils.setFieldValue("modulemo", dictDatamodulemo, model);
		CommonUtils.setFieldValue("moduletype", dictDatamoduletype, model);
		CommonUtils.setFieldValue("instrument_type", dictDatainstrumenttype, model);
		setAttr("model", model);
		render("detail.html");
	}

	public void lendPage() {
		setAttr("idlist", getPara("idlist"));
		setAttr("dictDataUser", Material.me.getDictDataUser());
		render("lend.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void lend() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		Record record = null;
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Instrument model = (Instrument) Instrument.me.findById(id);
			record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "2")
					.set("update_time", timestamp).remove("id");
			Db.save("instrument_log", record);
			model.set("borrower", getPara("model.borrower"));
			model.set("user", getPara("model.user"));
			model.set("outfrom", getPara("model.outfrom"));
			model.set("outtill", getPara("model.outtill"));
			model.set("project", getPara("model.project"));
			model.set("status", "out");
			CommonUtils.setFieldValue("user", "user_id", dictDataUser, model);
			CommonUtils.setFieldValue("borrower", "borrower_id", dictDataUser, model);
			model.update();
		}
		addOpLog("[????????????] ??????");
		renderSuccess();
	}

	public void picpage() {
		Instrument model = (Instrument) Instrument.me.findById(getPara("id"));
		Map<String, Object> dictDatalocation = Instrument.me.getDictDatalocation();
		CommonUtils.setFieldValue("location", dictDatalocation, model);
		setAttr("model", model);
		render("pic.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importpic() {
		String id = getPara("id");
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "????????????????????????...");
		Map<String, Object> result = new HashMap<String, Object>();
		String baPath = String.valueOf(PathKit.getWebRootPath()) + File.separator + "res" + File.separator + "upload"
				+ File.separator + "pic" + File.separator + "instrument";
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
		Instrument model = (Instrument) Instrument.me.findById(id);
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "5").remove("id");
		Db.save("instrument_log", record);
		model.set("pic", String.valueOf(id) + "." + suffix);
		model.update();
		addOpLog("[????????????]??????????????????");
		result.put("result", "success");
		result.put("msg", "????????????????????????!");
		render((new JsonRender(result)).forIE());
	}

	public void stockPage() {
		Instrument model = (Instrument) Instrument.me.findById(getPara("id"));
		Map<String, Object> dictDatalocation = Instrument.me.getDictDatalocation();
		Map<String, Object> dictDatamodulemo = Instrument.me.getDictDatamodulemo();
		Map<String, Object> dictDatamoduletype = Instrument.me.getDictDatamoduletype();
		CommonUtils.setFieldValue("location", dictDatalocation, model);
		CommonUtils.setFieldValue("modulemo", dictDatamodulemo, model);
		CommonUtils.setFieldValue("moduletype", dictDatamoduletype, model);
		setAttr("dictDataUser", Material.me.getDictDataUser());
		setAttr("model", model);
		render("stock.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void back() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Instrument model = (Instrument) Instrument.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "3")
					.set("update_time", timestamp).remove("id");
			Db.save("instrument_log", record);
		}
		String sql = "UPDATE INSTRUMENT SET outtill = NULL, user=null,outfrom = NULL,status='free' WHERE id IN ("
				+ getPara("idlist") + ")";
		Db.update(sql);
		addOpLog("[??????] ??????");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void stock() {
		Instrument model = (Instrument) Instrument.me.findById(getPara("id"));
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "4")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("instrument_log", record);
		model.set("stock_exist", getPara("model.stock_exist"));
		model.set("stock_time", getPara("model.stock_time"));
		model.set("stock_man", getPara("model.stock_man"));
		model.set("stock_remark", getPara("model.stock_remark"));
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("stock_man", dictDataUser, model);
		model.update();
		addOpLog("[??????] ??????");
		renderSuccess();
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
		String mailText = "???????????????????????????????????????<br>";
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Instrument model = (Instrument) Instrument.me.findById(id);
			record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "7")
					.set("update_time", timestamp).remove("id");
			Db.save("instrument_log", record);
			model.set("appointment_id", getSessionUser().getStr("id"));
			model.set("appointment_name", getSessionUser().getStr("display_name"));
			model.set("appointment_start", getPara("model.appointment_start"));
			model.set("appointment_end", getPara("model.appointment_end"));
			model.set("appointment_remark", getPara("model.appointment_remark"));
			model.set("appointment_save_time", timestamp);
			model.update();
			String locationValue = Material.me.getDictDataLocation(model.get("location").toString());
			mailText = String.valueOf(mailText) + "instrument_type????????????:" + model.get("instrument_type")
					+ "; Location??????:" + locationValue + "; IP:" + model.get("ip") + "; modulemo????????????:"
					+ model.get("modulemo") + "; moduletype????????????:" + model.get("moduletype") + "; port??????:"
					+ model.get("port") + " <br>";
		}
		mailText = String.valueOf(mailText) + "<br>";
		mailText = String.valueOf(mailText) + "???????????????:" + getPara("model.appointment_start") + "<br>";
		mailText = String.valueOf(mailText) + "???????????????:" + getPara("model.appointment_end") + "<br>";
		mailText = String.valueOf(mailText) + "????????????:" + getPara("model.appointment_remark") + "<br>";
		mailText = String.valueOf(mailText) + "?????????:" + getSessionUser().getStr("display_name") + "<br>";
		MailUtil.notifyMail(String.valueOf(getSessionUser().getStr("display_name")) + "???????????????", mailText);
		addOpLog("[????????????] ??????");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void clearAppointment() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Instrument model = (Instrument) Instrument.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "6")
					.set("update_time", timestamp).remove("id");
			Db.save("instrument_log", record);
		}
		String sql = "UPDATE instrument SET appointment_id = NULL, appointment_name = null, appointment_start=null,appointment_end = NULL, appointment_remark = null,appointment_save_time=null WHERE id IN ("
				+ getPara("idlist") + ")";
		Db.update(sql);
		addOpLog("[??????] ??????????????????");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "??????????????????????????????...");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		result.put("msg", "???????????????");
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
				result.put("msg", "????????????????????????????????????");
			} else {
				for (int i = 0; i < ruleExcelListList.size(); i++)
					ruleMap.put(Integer.valueOf(i),
							(String[]) ((List) ruleExcelListList.get(i)).toArray(new String[0]));
				fis1 = new FileInputStream(uploadFile);
				bis1 = new BufferedInputStream(fis1);
				List<List<String>> readExcel2List = ExcelUtils.getInstance().readExcel2List(bis1, 1);
				if (readExcel2List == null || readExcel2List.isEmpty()) {
					result.put("result", "fail");
					result.put("msg", "??????????????????????????????????????????");
				} else {
					List<Record> modulemoList = Db.find(getDictSql("????????????"));
					List<Record> find = Db
							.find("select id as k ,auto_val as v from sys_address_dict  where `status`='1'");
					List<Record> find3 = Db.find(getDictSql("??????"));
					List<Record> find4 = Db.find(getDictSql("????????????"));
					List<Record> find5 = Db.find(getDictSql("????????????"));
					if (find == null || find.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "????????????????????????????????????????????????????????????");
					} else if (find3 == null || find3.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "????????????????????????????????????????????????");
					} else if (find4 == null || find4.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "??????????????????????????????????????????????????????");
					} else if (find5 == null || find5.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "??????????????????????????????????????????????????????");
					} else if (modulemoList == null || modulemoList.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "??????????????????????????????????????????????????????");
					} else {
						Map<String, Long> rackAddressMap = new HashMap<String, Long>();
						for (int i = 0; i < find.size(); i++) {
							Record record = (Record) find.get(i);
							rackAddressMap.put(record.getStr("v"), record.getLong("k"));
						}
						Map<String, String> statusMap = CommonUtils.recordListToMap(find3);
						Map<String, String> mouduletypeMap = CommonUtils.recordListToMap(find4);
						Map<String, String> modulemoMap = CommonUtils.recordListToMap(modulemoList);
						Map<String, String> instrumenttypeMap = CommonUtils.recordListToMap(find5);
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
									listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
								}
								if (valiflag && StringUtils.isNotEmpty(val)) {
									Object v = null;
									if (strings[2].startsWith("Long")) {
										try {
											v = Utils.str2TargetClass(val, Long.class);
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
										}
									} else if (strings[2].startsWith("Date")) {
										try {
											v = Utils.str2TargetClass(val, Date.class);
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
										}
									} else if (strings[2].startsWith("Integer")) {
										try {
											v = Utils.str2TargetClass(val, Integer.class);
											String[] split = strings[2].split(":");
											if (split.length > 1) {
												valiflag = (((Integer) v).intValue() >= Integer.parseInt(split[1]));
												if (!valiflag) {
													listResult.add("Excel" + (i + 2) + "???" + strings[0] + "???????????????"
															+ Integer.parseInt(split[1]));
												} else if (split.length == 3) {
													valiflag = (((Integer) v).intValue() <= Integer.parseInt(split[2]));
													if (!valiflag)
														listResult.add("Excel" + (i + 2) + "???" + strings[0] + "???????????????"
																+ Integer.parseInt(split[2]));
												}
											}
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
										}
									} else {
										v = val;
									}
									if (valiflag) {
										switch (j) {
										case 0:
											if (instrumenttypeMap.containsKey(v)) {
												v = instrumenttypeMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "??????????????????????????????????????????");
											break;
										case 1:
											if (rackAddressMap.containsKey(v)) {
												v = rackAddressMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "????????????????????????????????????");
											break;
										case 7:
											if (modulemoMap.containsKey(v)) {
												v = modulemoMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "??????????????????????????????????????????");
											break;
										case 8:
											if (mouduletypeMap.containsKey(v)) {
												v = mouduletypeMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "??????????????????????????????????????????");
											break;
										case 10:
											if (statusMap.containsKey(v)) {
												v = statusMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "????????????????????????????????????");
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
								Db.save("instrument", (Record) batchRecord.get(i));
							result.put("result", "success");
							result.put("msg", "????????????,?????????" + batchRecord.size() + "?????????");
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "??????????????????????????????????????????"
									+ batchRecord.size() + "?????????");
						} else {
							result.put("result", "fail");
							result.put("msg", "????????????!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "????????????????????????????????????");
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("?????????????????????", e);
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error("??????Excel?????????????????????,??????????????????????????????", e);
			result.put("result", "fail");
			result.put("msg", "??????Excel?????????????????????????????????????????????????????????[" + e.getMessage() + "]");
		} finally {
			IOUtils.closeQuietly(new Closeable[] { fis, bis, fis1, bis1 });
			uploadFile.delete();
		}
		render((new JsonRender(result)).forIE());
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel2() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "??????????????????????????????...");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		result.put("msg", "???????????????");
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
				result.put("msg", "????????????????????????????????????");
			} else {
				for (int i = 0; i < ruleExcelListList.size(); i++)
					ruleMap.put(Integer.valueOf(i),
							(String[]) ((List) ruleExcelListList.get(i)).toArray(new String[0]));
				fis1 = new FileInputStream(uploadFile);
				bis1 = new BufferedInputStream(fis1);
				List<List<String>> readExcel2List = ExcelUtils.getInstance().readExcel2List(bis1, 1);
				if (readExcel2List == null || readExcel2List.isEmpty()) {
					result.put("result", "fail");
					result.put("msg", "??????????????????????????????????????????");
				} else {
					List<Record> find = Db
							.find("select id as k ,auto_val as v from sys_address_dict  where `status`='1'");
					List<Record> find5 = Db.find(getDictSql("????????????"));
					if (find == null || find.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "????????????????????????????????????????????????????????????");
					} else if (find5 == null || find5.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "??????????????????????????????????????????????????????");
					} else {
						Map<String, Long> rackAddressMap = new HashMap<String, Long>();
						for (int i = 0; i < find.size(); i++) {
							Record record = (Record) find.get(i);
							rackAddressMap.put(record.getStr("v"), record.getLong("k"));
						}
						Map<String, String> instrumenttypeMap = CommonUtils.recordListToMap(find5);
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
									listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
								}
								if (valiflag && StringUtils.isNotEmpty(val)) {
									Object v = null;
									if (strings[2].startsWith("Long")) {
										try {
											v = Utils.str2TargetClass(val, Long.class);
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
										}
									} else if (strings[2].startsWith("Date")) {
										try {
											v = Utils.str2TargetClass(val, Date.class);
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
										}
									} else if (strings[2].startsWith("Integer")) {
										try {
											v = Utils.str2TargetClass(val, Integer.class);
											String[] split = strings[2].split(":");
											if (split.length > 1) {
												valiflag = (((Integer) v).intValue() >= Integer.parseInt(split[1]));
												if (!valiflag) {
													listResult.add("Excel" + (i + 2) + "???" + strings[0] + "???????????????"
															+ Integer.parseInt(split[1]));
												} else if (split.length == 3) {
													valiflag = (((Integer) v).intValue() <= Integer.parseInt(split[2]));
													if (!valiflag)
														listResult.add("Excel" + (i + 2) + "???" + strings[0] + "???????????????"
																+ Integer.parseInt(split[2]));
												}
											}
										} catch (Exception e) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
										}
									} else {
										v = val;
									}
									if (valiflag) {
										switch (j) {
										case 0:
											if (instrumenttypeMap.containsKey(v)) {
												v = instrumenttypeMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "??????????????????????????????????????????");
											break;
										case 1:
											if (rackAddressMap.containsKey(v)) {
												v = rackAddressMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "???" + strings[0] + "????????????????????????????????????");
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
							Db.batchSave("instrument", batchRecord, 1000);
							result.put("result", "success");
							result.put("msg", "????????????,?????????" + batchRecord.size() + "?????????");
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "??????????????????????????????????????????"
									+ batchRecord.size() + "?????????");
						} else {
							result.put("result", "fail");
							result.put("msg", "????????????!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "????????????????????????????????????");
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("?????????????????????", e);
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error("??????Excel?????????????????????,??????????????????????????????", e);
			result.put("result", "fail");
			result.put("msg", "??????Excel?????????????????????????????????????????????????????????[" + e.getMessage() + "]");
		} finally {
			IOUtils.closeQuietly(new Closeable[] { fis, bis, fis1, bis1 });
			uploadFile.delete();
		}
		render((new JsonRender(result)).forIE());
	}

	public void expExcel() {
		String select = "select a.* ";
		List<Record> paginate2 = Db.find(String.valueOf(select) + sqlExceptSelect());
		Map<String, Object> dictDatalocation = Instrument.me.getDictDatalocation();
		Map<String, Object> dictDatastatus = Instrument.me.getDictDatastatus();
		Map<String, Object> dictDatamodulemo = Instrument.me.getDictDatamodulemo();
		Map<String, Object> dictDatamoduletype = Instrument.me.getDictDatamoduletype();
		Map<String, Object> dictDatainstrumenttype = Instrument.me.getDictDatainstrumenttype();
		for (Record record : paginate2) {
			CommonUtils.setFieldValue("location", dictDatalocation, record);
			CommonUtils.setFieldValue("status", dictDatastatus, record);
			CommonUtils.setFieldValue("modulemo", dictDatamodulemo, record);
			CommonUtils.setFieldValue("moduletype", dictDatamoduletype, record);
			CommonUtils.setFieldValue("instrument_type", dictDatainstrumenttype, record);
		}
		String fileName = "??????" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
		String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
		System.out.println(path);
		File file = new File(path);
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("??????");
		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("????????????");
		row0.createCell(1).setCellValue("??????");
		row0.createCell(2).setCellValue("????????????");
		row0.createCell(3).setCellValue("??????????????????");
		row0.createCell(4).setCellValue("????????????");
		row0.createCell(5).setCellValue("??????????????????");
		row0.createCell(6).setCellValue("IP");
		row0.createCell(7).setCellValue("????????????");
		row0.createCell(8).setCellValue("????????????");
		row0.createCell(9).setCellValue("??????");
		row0.createCell(10).setCellValue("??????");
		row0.createCell(11).setCellValue("??????");
		row0.createCell(12).setCellValue("????????????");
		row0.createCell(13).setCellValue("SN");
		row0.createCell(14).setCellValue("?????????");
		row0.createCell(15).setCellValue("?????????");
		row0.createCell(16).setCellValue("?????????");
		row0.createCell(17).setCellValue("?????????");
		row0.createCell(18).setCellValue("??????");
		row0.createCell(19).setCellValue("????????????");
		row0.createCell(20).setCellValue("???????????????");
		row0.createCell(21).setCellValue("????????????");
		row0.createCell(22).setCellValue("?????????");
		row0.createCell(23).setCellValue("????????????");
		row0.createCell(24).setCellValue("????????????");
		row0.createCell(25).setCellValue("??????");
		row0.createCell(26).setCellValue("???????????????");
		row0.createCell(27).setCellValue("???????????????");
		row0.createCell(28).setCellValue("???????????????");
		row0.createCell(29).setCellValue("????????????");
		for (int i = 0; i < paginate2.size(); i++) {
			Record record2 = (Record) paginate2.get(i);
			HSSFRow rowi = sheet.createRow(i + 1);
			rowi.createCell(0).setCellValue(isNull(record2.get("instrument_type")));
			rowi.createCell(1).setCellValue(isNull(record2.get("location")));
			rowi.createCell(2).setCellValue(isNull(record2.get("date")));
			rowi.createCell(3).setCellValue(isNull(record2.get("mdf")));
			rowi.createCell(4).setCellValue(isNull(record2.get("fiber")));
			rowi.createCell(5).setCellValue(isNull(record2.get("odf")));
			rowi.createCell(6).setCellValue(isNull(record2.get("ip")));
			rowi.createCell(7).setCellValue(isNull(record2.get("modulemo")));
			rowi.createCell(8).setCellValue(isNull(record2.get("moduletype")));
			rowi.createCell(9).setCellValue(isNull(record2.get("port")));
			rowi.createCell(10).setCellValue(isNull(record2.get("status")));
			rowi.createCell(11).setCellValue(isNull(record2.get("model")));
			rowi.createCell(12).setCellValue(isNull(record2.get("assetno")));
			rowi.createCell(13).setCellValue(isNull(record2.get("sn")));
			rowi.createCell(14).setCellValue(isNull(record2.get("user")));
			rowi.createCell(15).setCellValue(isNull(record2.get("borrower")));
			rowi.createCell(16).setCellValue(isNull(record2.get("outfrom")));
			rowi.createCell(17).setCellValue(isNull(record2.get("outtill")));
			rowi.createCell(18).setCellValue(isNull(record2.get("project")));
			rowi.createCell(19).setCellValue(isNull(record2.get("modulesn")));
			rowi.createCell(20).setCellValue(isNull(record2.get("owner")));
			rowi.createCell(21).setCellValue(escapeCode((String) record2.get("stock_exist")));
			rowi.createCell(22).setCellValue(isNull(record2.get("stock_man")));
			rowi.createCell(23).setCellValue(isNull(record2.get("stock_time")));
			rowi.createCell(24).setCellValue(isNull(record2.get("stock_remark")));
			rowi.createCell(25).setCellValue(isNull(record2.get("remark")));
			rowi.createCell(26).setCellValue(isNull(record2.get("appointment_name")));
			rowi.createCell(27).setCellValue(appointmentTime(record2.get("appointment_start")));
			rowi.createCell(28).setCellValue(appointmentTime(record2.get("appointment_end")));
			rowi.createCell(29).setCellValue(isNull(record2.get("appointment_remark")));
		}
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(file);
			this.wb.write(fileOut);
			renderFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			renderSuccess("????????????!");
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
