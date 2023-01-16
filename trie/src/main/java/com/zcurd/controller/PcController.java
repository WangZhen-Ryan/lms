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
import com.zcurd.model.Material;
import com.zcurd.model.Pc;
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

public class PcController extends BaseController {
	public void listPage() {
		setAttr("dictDataLocation", Pc.me.getDictDataLocation());
		setAttr("dictDatapc_property", Pc.me.getDictDatapc_property());
		render("list.html");
	}

	public void listData() {
		String select = "select a.* ";
		Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
		Map<String, Object> dictDataLocation = Pc.me.getDictDataLocation();
		Map<String, Object> dictDataPcProperty = Pc.me.getDictDatapc_property();
		for (Record record : paginate.getList()) {
			CommonUtils.setFieldValue("pc_property", dictDataPcProperty, record);
			CommonUtils.setFieldValue("Location", dictDataLocation, record);
		}
		renderDatagrid(paginate);
	}

	private String sqlExceptSelect() {
		String sqlSelect = getSearchText("pc");
		String sqlExceptSelect = " from pc a,sys_address_dict b where a.location  = b.id ";
		String location = getPara("Location");
		String pc_property = getPara("pc_property");
		String appointment_name = getPara("appointment_name");
		String user = getPara("user");
		String project = getPara("Project");
		if (StringUtils.isNotEmpty(pc_property))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.pc_property = '" + pc_property + "'";
		if (StringUtils.isNotEmpty(location))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'";
		if (StringUtils.isNoneEmpty(new CharSequence[] { appointment_name }))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.appointment_name ='" + appointment_name + "'";
		if (StringUtils.isNotEmpty(user))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.user like '%" + user + "%'";
		if (StringUtils.isNotEmpty(sqlSelect))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and (" + sqlSelect + ")";
		if (StringUtils.isNotEmpty(project))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.project like '%" + project + "%'";
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
		setAttr("dictDataLocation", Pc.me.getDictDataLocation());
		setAttr("dictDatapc_property", Pc.me.getDictDatapc_property());
		setAttr("dictDataUser", Material.me.getDictDataUser());
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		Pc pc = (Pc) getModel(Pc.class, "model");
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("user", "user_id", dictDataUser, pc);
		CommonUtils.setFieldValue("Project", "Project_id", dictDataUser, pc);
		pc.save();
		addOpLog("[PC] 增加");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDataLocation", Pc.me.getDictDataLocation());
		setAttr("dictDatapc_property", Pc.me.getDictDatapc_property());
		setAttr("dictDataUser", Material.me.getDictDataUser());
		Pc model = (Pc) Pc.me.findById(getPara("id"));
		if (!Check.IsStringNULL((String) model.get("user_id")))
			model.set("user", model.get("user_id"));
		if (!Check.IsStringNULL((String) model.get("Project_id")))
			model.set("Project", model.get("Project_id"));
		setAttr("model", model);
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		Pc model = (Pc) Pc.me.findById(getPara("id"));
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "0")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("pc_log", record);
		model.set("Location", getPara("model.Location"));
		model.set("SN", getPara("model.SN"));
		model.set("Access", getPara("model.Access"));
		model.set("Owner", getPara("model.Owner"));
		model.set("pc_property", getPara("model.pc_property"));
		model.set("IP", getPara("model.IP"));
		model.set("MAC", getPara("model.MAC"));
		model.set("Package", getPara("model.Package"));
		model.set("virus", getPara("model.virus"));
		model.set("scan_virus", getPara("model.scan_virus"));
		model.set("op_system", getPara("model.op_system"));
		model.set("reinstall", getPara("model.reinstall"));
		model.set("infected", getPara("model.infected"));
		model.set("precaution", getPara("model.precaution"));
		model.set("user", getPara("model.user"));
		model.set("sign", getPara("model.sign"));
		model.set("contact_way", getPara("model.contact_way"));
		model.set("OUT_from", getPara("model.OUT_from"));
		model.set("OUT_till", getPara("model.OUT_till"));
		model.set("Project", getPara("model.Project"));
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("user", "user_id", dictDataUser, model);
		CommonUtils.setFieldValue("Project", "Project_id", dictDataUser, model);
		model.update();
		addOpLog("[PC] 修改");
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
			Pc model = (Pc) Pc.me.findById(id);
			((Pc) (new Pc()).set("id", id)).delete();

			Record record = new Record();
			record.setColumns((Model) model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "1")
					.set("update_time", timestamp).remove("id");
			Db.save("pc_log", record);
			b++;
		}
		addOpLog("[PC] 删除");
		renderSuccess();
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
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Pc model = (Pc) Pc.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "2")
					.set("update_time", timestamp).remove("id");
			Db.save("pc_log", record);
			model.set("user", getPara("model.user"));
			model.set("OUT_from", getPara("model.OUT_from"));
			model.set("OUT_till", getPara("model.OUT_till"));
			model.set("Project", getPara("model.Project"));
			CommonUtils.setFieldValue("user", "user_id", dictDataUser, model);
			CommonUtils.setFieldValue("Project", "Project_id", dictDataUser, model);
			model.update();
		}
		addOpLog("[PC] 借出");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void back() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Pc model = (Pc) Pc.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "3")
					.set("update_time", timestamp).remove("id");
			Db.save("pc_log", record);
		}
		Db.update("UPDATE pc SET OUT_till = NULL, user = null,user_id = null, OUT_from = NULL WHERE id IN ("
				+ getPara("idlist") + ")");
		addOpLog("[PC] 归还");
		renderSuccess();
	}

	private static Map<String, Object> staticMap = new HashMap<String, Object>() {
		private static final long serialVersionUID = 1L;
	};

	private static Map<String, Object> staticMap1 = new HashMap<String, Object>() {
		private static final long serialVersionUID = 1L;
	};
	
	private HSSFWorkbook wb;

	public void detailPage() {
		Pc model = (Pc) Pc.me.findById(getParaToLong("id"));
		Map<String, Object> dictDataLocation = Pc.me.getDictDataLocation();
		Map<String, Object> dictDataPcProperty = Pc.me.getDictDatapc_property();
		CommonUtils.setFieldValue("pc_property", dictDataPcProperty, model);
		CommonUtils.setFieldValue("Location", dictDataLocation, model);
		CommonUtils.setFieldValue("Package", staticMap1, model);
		CommonUtils.setFieldValue("virus", staticMap1, model);
		CommonUtils.setFieldValue("scan_virus", staticMap1, model);
		CommonUtils.setFieldValue("reinstall", staticMap1, model);
		CommonUtils.setFieldValue("infected", staticMap1, model);
		CommonUtils.setFieldValue("sign", staticMap1, model);
		CommonUtils.setFieldValue("stock_exist", staticMap1, model);
		setAttr("model", model);
		render("detail.html");
	}

	public void picpage() {
		Pc model = (Pc) Pc.me.findById(getPara("id"));
		Map<String, Object> dictDataLocation = Pc.me.getDictDataLocation();
		Map<String, Object> dictDataPcProperty = Pc.me.getDictDatapc_property();
		CommonUtils.setFieldValue("pc_property", dictDataPcProperty, model);
		CommonUtils.setFieldValue("Location", dictDataLocation, model);
		setAttr("model", model);
		render("pic.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importpic() {
		String id = getPara("id");
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传PC照片...");
		Map<String, Object> result = new HashMap<String, Object>();
		String baPath = String.valueOf(PathKit.getWebRootPath()) + File.separator + "res" + File.separator + "upload"
				+ File.separator + "pic" + File.separator + "pc";
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
		Pc model = (Pc) Pc.me.findById(id);
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "5")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("pc_log", record);
		model.set("pic", String.valueOf(id) + "." + suffix);
		model.update();
		addOpLog("[PC]上传照片");
		result.put("result", "success");
		result.put("msg", "上传PC照片成功!");
		render((new JsonRender(result)).forIE());
	}

	public void stockPage() {
		Pc model = (Pc) Pc.me.findById(getPara("id"));
		Map<String, Object> dictDataLocation = Pc.me.getDictDataLocation();
		Map<String, Object> dictDataPcProperty = Pc.me.getDictDatapc_property();
		CommonUtils.setFieldValue("pc_property", dictDataPcProperty, model);
		CommonUtils.setFieldValue("Location", dictDataLocation, model);
		setAttr("dictDataUser", Material.me.getDictDataUser());
		setAttr("model", model);
		render("stock.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void stock() {
		Pc model = (Pc) Pc.me.findById(getPara("id"));
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "4")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("pc_log", record);
		model.set("stock_exist", getPara("model.stock_exist"));
		model.set("stock_time", getPara("model.stock_time"));
		model.set("stock_man", getPara("model.stock_man"));
		model.set("stock_remark", getPara("model.stock_remark"));
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("stock_man", dictDataUser, model);
		model.update();
		addOpLog("[PC] 盘点");
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
		String mailText = "注意您有新的PC预约申请！<br>";
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Pc model = (Pc) Pc.me.findById(id);
			record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "7")
					.set("update_time", timestamp).remove("id");
			Db.save("pc_log", record);
			model.set("appointment_id", getSessionUser().get("id"));
			model.set("appointment_name", getSessionUser().getStr("display_name"));
			model.set("appointment_start", getPara("model.appointment_start"));
			model.set("appointment_end", getPara("model.appointment_end"));
			model.set("appointment_remark", getPara("model.appointment_remark"));
			model.set("appointment_save_time", timestamp);
			model.update();
			String locationValue = Material.me.getDictDataLocation(model.get("Location").toString());
			mailText = String.valueOf(mailText) + "Location位置:" + locationValue + "; SN序列号:" + model.get("SN")
					+ "; Owner所有人:" + model.get("Owner") + "; IP:" + model.get("IP") + "; MAC地址或备注:" + model.get("MAC")
					+ "<br>";
		}
		mailText = String.valueOf(mailText) + "<br>";
		mailText = String.valueOf(mailText) + "预约日期起:" + getPara("model.appointment_start") + "<br>";
		mailText = String.valueOf(mailText) + "预约日期止:" + getPara("model.appointment_end") + "<br>";
		mailText = String.valueOf(mailText) + "预约备注:" + getPara("model.appointment_remark") + "<br>";
		mailText = String.valueOf(mailText) + "预约人:" + getSessionUser().getStr("display_name") + "<br>";
		MailUtil.notifyMail(String.valueOf(getSessionUser().getStr("display_name")) + "的预约提醒", mailText);
		addOpLog("[PC] 预约");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void clearAppointment() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Pc model = (Pc) Pc.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "6")
					.set("update_time", timestamp).remove("id");
			Db.save("pc_log", record);
		}
		String sql = "UPDATE pc SET appointment_id = NULL, appointment_name = null, appointment_start=null,appointment_end = NULL, appointment_remark = null,appointment_save_time=null WHERE id IN ("
				+ getPara("idlist") + ")";
		Db.update(sql);
		addOpLog("[PC] 预约记录清除");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传PC信息文件...");
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
					List<Record> find = Db
							.find("select id as k ,auto_val as v from sys_address_dict  where `status`='1'");
					List<Record> find1 = Db.find(getDictSql("PC属性"));
					if (find == null || find.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护地址字典菜单中地址编码信息！");
					} else if (find1 == null || find1.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护数据字典的PC属性信息！");
					} else {
						Map<String, Long> rackAddressMap = new HashMap<String, Long>();
						for (int i = 0; i < find.size(); i++) {
							Record record = (Record) find.get(i);
							rackAddressMap.put(record.getStr("v"), record.getLong("k"));
						}
						Map<String, String> pcPropertyMap = CommonUtils.recordListToMap(find1);
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
											if (rackAddressMap.containsKey(v)) {
												v = rackAddressMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于位置字典中");
											break;
										case 4:
											if (pcPropertyMap.containsKey(v)) {
												v = pcPropertyMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于PC属性字典中");
											break;
										case 7:
										case 8:
										case 9:
										case 11:
										case 12:
										case 15:
											if (staticMap.containsKey(v)) {
												v = staticMap.get(v);
												break;
											}
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据必须填写有或无");
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
								Db.save("pc", (Record) batchRecord.get(i));
							result.put("result", "success");
							result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传PC信息文件结束，共入库"
									+ batchRecord.size() + "条数据");
						} else {
							result.put("result", "fail");
							result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传PC信息文件异常结束");
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
		Map<String, Object> dictDataLocation = Pc.me.getDictDataLocation();
		Map<String, Object> dictDataPcProperty = Pc.me.getDictDatapc_property();
		for (Record record : paginate2) {
			CommonUtils.setFieldValue("pc_property", dictDataPcProperty, record);
			CommonUtils.setFieldValue("Location", dictDataLocation, record);
		}
		String fileName = "PC表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
		String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
		System.out.println(path);
		File file = new File(path);
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("PC表");
		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("位置");
		row0.createCell(1).setCellValue("序列号");
		row0.createCell(2).setCellValue("资产编号");
		row0.createCell(3).setCellValue("所有人");
		row0.createCell(4).setCellValue("PC属性");
		row0.createCell(5).setCellValue("IP");
		row0.createCell(6).setCellValue("MAC地址或备注");
		row0.createCell(7).setCellValue("打补丁");
		row0.createCell(8).setCellValue("杀毒软件");
		row0.createCell(9).setCellValue("查毒");
		row0.createCell(10).setCellValue("系统");
		row0.createCell(11).setCellValue("重装");
		row0.createCell(12).setCellValue("中毒");
		row0.createCell(13).setCellValue("措施");
		row0.createCell(14).setCellValue("使用人");
		row0.createCell(15).setCellValue("签字");
		row0.createCell(16).setCellValue("联系方式");
		row0.createCell(17).setCellValue("借用日");
		row0.createCell(18).setCellValue("归还日");
		row0.createCell(19).setCellValue("申请人");
		row0.createCell(20).setCellValue("是否存在");
		row0.createCell(21).setCellValue("盘点人");
		row0.createCell(22).setCellValue("盘点时间");
		row0.createCell(23).setCellValue("盘点注释");
		row0.createCell(24).setCellValue("预约人姓名");
		row0.createCell(25).setCellValue("预约日期起");
		row0.createCell(26).setCellValue("预约日期止");
		row0.createCell(27).setCellValue("预约备注");
		for (int i = 0; i < paginate2.size(); i++) {
			Record record2 = (Record) paginate2.get(i);
			HSSFRow rowi = sheet.createRow(i + 1);
			rowi.createCell(0).setCellValue(isNull(record2.get("Location")));
			rowi.createCell(1).setCellValue(isNull(record2.get("SN")));
			rowi.createCell(2).setCellValue(isNull(record2.get("Access")));
			rowi.createCell(3).setCellValue(isNull(record2.get("Owner")));
			rowi.createCell(4).setCellValue(isNull(record2.get("pc_property")));
			rowi.createCell(5).setCellValue(isNull(record2.get("IP")));
			rowi.createCell(6).setCellValue(isNull(record2.get("MAC")));
			rowi.createCell(7).setCellValue(escapeCode((String) record2.get("Package")));
			rowi.createCell(8).setCellValue(escapeCode((String) record2.get("virus")));
			rowi.createCell(9).setCellValue(escapeCode((String) record2.get("scan_virus")));
			rowi.createCell(10).setCellValue(isNull(record2.get("op_system")));
			rowi.createCell(11).setCellValue(escapeCode((String) record2.get("reinstall")));
			rowi.createCell(12).setCellValue(escapeCode((String) record2.get("infected")));
			rowi.createCell(13).setCellValue(isNull(record2.get("precaution")));
			rowi.createCell(14).setCellValue(isNull(record2.get("user")));
			rowi.createCell(15).setCellValue(escapeCode((String) record2.get("sign")));
			rowi.createCell(16).setCellValue(isNull(record2.get("contact_way")));
			rowi.createCell(17).setCellValue(isNull(record2.get("OUT_from")));
			rowi.createCell(18).setCellValue(isNull(record2.get("OUT_till")));
			rowi.createCell(19).setCellValue(isNull(record2.get("Project")));
			rowi.createCell(20).setCellValue(escapeCode((String) record2.get("stock_exist")));
			rowi.createCell(21).setCellValue(isNull(record2.get("stock_man")));
			String stock_time = isNull(record2.get("stock_time"));
			rowi.createCell(22).setCellValue((stock_time.length() > 10) ? stock_time.substring(0, 10) : stock_time);
			rowi.createCell(23).setCellValue(isNull(record2.get("stock_remark")));
			rowi.createCell(24).setCellValue(isNull(record2.get("appointment_name")));
			rowi.createCell(25).setCellValue(appointmentTime(record2.get("appointment_start")));
			rowi.createCell(26).setCellValue(appointmentTime(record2.get("appointment_end")));
			rowi.createCell(27).setCellValue(isNull(record2.get("appointment_remark")));
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
