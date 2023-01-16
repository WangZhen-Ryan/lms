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
import com.zcurd.model.Material;
import com.zcurd.model.Platform;
import com.zcurd.model.Rack;
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

public class PlatformController extends BaseController {
	private HSSFWorkbook wb;

	public void listPage() {
		setAttr("dictDatastatus", Platform.me.getDictDatastatus());
		render("list.html");
	}

	public void listData() {
		String select = "select a.* ";
		Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
		Map<String, Object> dictDatalocation = Platform.me.getDictDatalocation();
		Map<String, Object> dictDatastatus = Platform.me.getDictDatastatus();
		for (Record record : paginate.getList()) {
			CommonUtils.setFieldValue("location", dictDatalocation, record);
			CommonUtils.setFieldValue("status", dictDatastatus, record);
		}
		renderDatagrid(paginate);
	}

	private String sqlExceptSelect() {
		String sqlSelect = getSearchText("platform");
		String sqlExceptSelect = " from platform a,sys_address_dict b where a.location  = b.id ";
		String location = getPara("location");
		String position = getPara("status");
		String project = getPara("project");
		String user = getPara("user");
		if (StringUtils.isNotEmpty(position))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.status = '" + position + "'";
		if (StringUtils.isNotEmpty(project))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.project like '%" + project + "%'";
		if (StringUtils.isNotEmpty(location))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'";
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
		setAttr("dictDatalocation", Platform.me.getDictDatalocation());
		setAttr("dictDatastatus", Platform.me.getDictDatastatus());
		setAttr("dictDataUser", Material.me.getDictDataUser());
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		Platform model = (Platform) getModel(Platform.class, "model");
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("user", dictDataUser, model);
		CommonUtils.setFieldValue("tpm", dictDataUser, model);
		CommonUtils.setFieldValue("fl_tpl", dictDataUser, model);
		model.save();
		addOpLog("[操作桌] 增加");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDatalocation", Platform.me.getDictDatalocation());
		setAttr("dictDatastatus", Platform.me.getDictDatastatus());
		setAttr("dictDataUser", Material.me.getDictDataUser());
		setAttr("model", Platform.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		Platform model = (Platform) Platform.me.findById(getPara("id"));
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "0").remove("id");
		Db.save("platform_log", record);
		model.set("location", getParaToLong("model.location"));
		model.set("status", getPara("model.status"));
		model.set("user", getPara("model.user"));
		model.set("pl_code", getPara("model.pl_code"));
		model.set("from_date", getPara("model.from_date"));
		model.set("till_date", getPara("model.till_date"));
		model.set("project", getPara("model.project"));
		model.set("sub_project", getPara("model.sub_project"));
		model.set("cc", getPara("model.cc"));
		model.set("tpm", getPara("model.tpm"));
		model.set("fl_tpl", getPara("model.fl_tpl"));
		model.set("net", getPara("model.net"));
		model.set("vlan", getPara("model.vlan"));
		model.set("switch_port", getPara("model.switch_port"));
		model.set("remark", getPara("model.remark"));
		model.set("ip_address", getPara("model.ip_address"));
		model.set("netmask", getPara("model.netmask"));
		model.set("gateway", getPara("model.gateway"));
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("user", dictDataUser, model);
		CommonUtils.setFieldValue("tpm", dictDataUser, model);
		CommonUtils.setFieldValue("fl_tpl", dictDataUser, model);
		model.update();
		addOpLog("[操作桌] 修改");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void delete() {
		Long[] ids = getParaValuesToLong("id[]");
		Record record = null;
		byte b;
		int i;
		Long[] arrayOfLong;
		for (i = (arrayOfLong = ids).length, b = 0; b < i;) {
			Long id = arrayOfLong[b];
			Platform model = (Platform) Platform.me.findById(id);
			((Platform) (new Platform()).set("id", id)).delete();

			record = new Record();
			record.setColumns((Model) model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "1").remove("id");
			Db.save("platform_log", record);
			b++;
		}
		addOpLog("[操作桌] 删除");
		renderSuccess();
	}

	public void detailPage() {
		Platform model = (Platform) Platform.me.findById(getParaToLong("id"));
		Map<String, Object> dictDatalocation = Platform.me.getDictDatalocation();
		Map<String, Object> dictDatastatus = Platform.me.getDictDatastatus();
		CommonUtils.setFieldValue("location", dictDatalocation, model);
		CommonUtils.setFieldValue("status", dictDatastatus, model);
		setAttr("model", model);
		render("detail.html");
	}

	public void picpage() {
		Platform model = (Platform) Platform.me.findById(getPara("id"));
		Map<String, Object> dictDatalocation = Rack.me.getDictDatalocation();
		Map<String, Object> dictDataposition = Rack.me.getDictDataposition();
		CommonUtils.setFieldValue("location", dictDatalocation, model);
		CommonUtils.setFieldValue("position", dictDataposition, model);
		setAttr("model", model);
		render("pic.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importpic() {
		String id = getPara("id");
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传操作桌照片...");
		Map<String, Object> result = new HashMap<String, Object>();
		String baPath = String.valueOf(PathKit.getWebRootPath()) + File.separator + "res" + File.separator + "upload"
				+ File.separator + "pic" + File.separator + "platform";
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
		Platform model = (Platform) Platform.me.findById(id);
		Record record = new Record();
		record.setColumns(model);
		record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "5")
				.set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
		Db.save("platform_log", record);
		model.set("pic", String.valueOf(id) + "." + suffix);
		model.update();
		addOpLog("[Platform]上传成功!");
		result.put("result", "success");
		result.put("msg", "上传操作桌照片成功!");
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
		String mailText = "注意您有新的操作桌预约申请！<br>";
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Platform model = (Platform) Platform.me.findById(id);
			record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "7")
					.set("update_time", timestamp).remove("id");
			Db.save("platform_log", record);
			model.set("appointment_id", getSessionUser().getStr("id"));
			model.set("appointment_name", getSessionUser().getStr("display_name"));
			model.set("appointment_start", getPara("model.appointment_start"));
			model.set("appointment_end", getPara("model.appointment_end"));
			model.set("appointment_remark", getPara("model.appointment_remark"));
			model.set("appointment_save_time", timestamp);
			model.update();
		}
		addOpLog("[操作桌管理] 预约");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void clearAppointment() {
		String[] idlist = getPara("idlist").split(",");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		for (int i = 0; i < idlist.length; i++) {
			String id = idlist[i];
			Platform model = (Platform) Platform.me.findById(id);
			Record record = new Record();
			record.setColumns(model);
			record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "6")
					.set("update_time", timestamp).remove("id");
			Db.save("platform_log", record);
		}
		String sql = "UPDATE platform SET appointment_id = NULL, appointment_name = null, appointment_start=null,appointment_end = NULL, appointment_remark = null,appointment_save_time=null WHERE id IN ("
				+ getPara("idlist") + ")";
		Db.update(sql);
		addOpLog("[操作桌] 预约记录清除");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传操作桌信息文件...");
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
					List<Record> find3 = Db.find(getDictSql("状态"));
					if (find == null || find.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护地址字典菜单中地址编码信息！");
					} else if (find3 == null || find3.isEmpty()) {
						result.put("result", "fail");
						result.put("msg", "请管理员维护数据字典的状态信息！");
					} else {
						Map<String, Long> rackAddressMap = new HashMap<String, Long>();
						for (int i = 0; i < find.size(); i++) {
							Record record = (Record) find.get(i);
							if ("B6C201C02-87".equals(record.getStr("v")))
								System.out.println("+++++++++++++++++++++++++++++++" + record.getStr("v"));
							rackAddressMap.put(record.getStr("v"), record.getLong("k"));
						}
						Map<String, String> statusMap = CommonUtils.recordListToMap(find3);
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
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于地址字典中");
											break;
										case 4:
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
								Db.save("platform", (Record) batchRecord.get(i));
							result.put("result", "success");
							result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传操作桌信息文件结束，共入库"
									+ batchRecord.size() + "条数据");
						} else {
							result.put("result", "fail");
							result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
							logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传操作桌信息文件异常结束");
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
		Map<String, Object> dictDatalocation = Platform.me.getDictDatalocation();
		Map<String, Object> dictDatastatus = Platform.me.getDictDatastatus();
		for (Record record : paginate2) {
			CommonUtils.setFieldValue("location", dictDatalocation, record);
			CommonUtils.setFieldValue("status", dictDatastatus, record);
		}
		String fileName = "操作桌表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
		String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
		File file = new File(path);
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("操作桌表");
		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("位置");
		row0.createCell(1).setCellValue("IP地址");
		row0.createCell(2).setCellValue("子网掩码");
		row0.createCell(3).setCellValue("网关");
		row0.createCell(4).setCellValue("状态");
		row0.createCell(5).setCellValue("使用人");
		row0.createCell(6).setCellValue("产品线代码");
		row0.createCell(7).setCellValue("起始日期");
		row0.createCell(8).setCellValue("结束日期");
		row0.createCell(9).setCellValue("项目");
		row0.createCell(10).setCellValue("子项目");
		row0.createCell(11).setCellValue("能力中心");
		row0.createCell(12).setCellValue("项目经理");
		row0.createCell(13).setCellValue("项目领导");
		row0.createCell(14).setCellValue("网络端口");
		row0.createCell(15).setCellValue("交换机VLAN");
		row0.createCell(16).setCellValue("交换机端口");
		row0.createCell(17).setCellValue("备注");
		row0.createCell(18).setCellValue("预约人姓名");
		row0.createCell(19).setCellValue("预约日期起");
		row0.createCell(20).setCellValue("预约日期止");
		row0.createCell(21).setCellValue("预约备注");
		for (int i = 0; i < paginate2.size(); i++) {
			Record record2 = (Record) paginate2.get(i);
			HSSFRow rowi = sheet.createRow(i + 1);
			rowi.createCell(0).setCellValue(isNull(record2.get("location")));
			rowi.createCell(1).setCellValue(isNull(record2.get("ip_address")));
			rowi.createCell(2).setCellValue(isNull(record2.get("netmask")));
			rowi.createCell(3).setCellValue(isNull(record2.get("gateway")));
			rowi.createCell(4).setCellValue(isNull(record2.get("status")));
			rowi.createCell(5).setCellValue(isNull(record2.get("user")));
			rowi.createCell(6).setCellValue(isNull(record2.get("pl_code")));
			rowi.createCell(7).setCellValue(isNull(record2.get("from_date")));
			rowi.createCell(8).setCellValue(isNull(record2.get("till_date")));
			rowi.createCell(9).setCellValue(isNull(record2.get("project")));
			rowi.createCell(10).setCellValue(isNull(record2.get("sub_project")));
			rowi.createCell(11).setCellValue(isNull(record2.get("cc")));
			rowi.createCell(12).setCellValue(isNull(record2.get("tpm")));
			rowi.createCell(13).setCellValue(isNull(record2.get("fl_tpl")));
			rowi.createCell(14).setCellValue(isNull(record2.get("net")));
			rowi.createCell(15).setCellValue(isNull(record2.get("vlan")));
			rowi.createCell(16).setCellValue(isNull(record2.get("switch_port")));
			rowi.createCell(17).setCellValue(isNull(record2.get("remark")));
			rowi.createCell(18).setCellValue(isNull(record2.get("appointment_name")));
			rowi.createCell(19).setCellValue(appointmentTime(record2.get("appointment_start")));
			rowi.createCell(20).setCellValue(appointmentTime(record2.get("appointment_end")));
			rowi.createCell(21).setCellValue(isNull(record2.get("appointment_remark")));
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
