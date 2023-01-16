package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.JsonRender;
import com.jfinal.upload.UploadFile;
import com.zcurd.common.util.Check;
import com.zcurd.common.util.CommonUtils;
import com.zcurd.common.util.ToolDateTime;
import com.zcurd.excel.ExcelUtils;
import com.zcurd.excel.utils.Utils;
import com.zcurd.model.Emd;
import com.zcurd.model.EmdOrderDetail;
import com.zcurd.model.Material;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class EmdController extends BaseController {
	private HSSFWorkbook wb;

	public void listPage() {
		render("list.html");
	}

	public void listData() {
		String select = "select a.* ";
		Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
		if (paginate.getList() != null) {
			String sql = "select sum(order_num) as order_total_num from emd_order_detail where emd_id = ?";
			for (Record record : paginate.getList()) {
				Object object = Db.findFirst(sql, new Object[] { record.getLong("id") }).get("order_total_num");
				if (object == null)
					object = Integer.valueOf(0);
				record.set("order_total_num", object);
			}
		}
		renderDatagrid(paginate);
	}

	public String sqlExceptSelect() {
		String sqlSelect = getSearchText("emd");
		String sqlExceptSelect = " from emd a where 1= 1 ";
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
		setAttr("dictDataUser", Material.me.getDictDataUser());
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		Emd model = (Emd) getModel(Emd.class, "model");
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("proposer", dictDataUser, model);
		CommonUtils.setFieldValue("maker", dictDataUser, model);
		model.save();
		addOpLog("[领料] 增加");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDataUser", Material.me.getDictDataUser());
		setAttr("model", Emd.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		Emd model = (Emd) Emd.me.findById(getPara("id"));
		model.set("emd_date", getPara("model.emd_date"));
		model.set("emd_no", getPara("model.emd_no"));
		model.set("po_no", getPara("model.po_no"));
		model.set("deadline", getPara("model.deadline"));
		model.set("proposer", getPara("model.proposer"));
		model.set("cc", getPara("model.cc"));
		model.set("lab", getPara("model.lab"));
		model.set("instrument_code", getPara("model.instrument_code"));
		model.set("instrument_name", getPara("model.instrument_name"));
		model.set("qty", getPara("model.qty"));
		model.set("unit_price", getPara("model.unit_price"));
		model.set("total", getPara("model.total"));
		model.set("project", getPara("model.project"));
		model.set("project_no", getPara("model.project_no"));
		model.set("cost_center", getPara("model.cost_center"));
		model.set("emd_stauts", getPara("model.emd_stauts"));
		model.set("real_date", getPara("model.real_date"));
		model.set("maker", getPara("model.maker"));
		model.set("remark", getPara("model.remark"));
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("proposer", dictDataUser, model);
		CommonUtils.setFieldValue("maker", dictDataUser, model);
		model.update();
		addOpLog("[领料] 修改");
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
			((Emd) (new Emd()).set("id", id)).delete();
			b++;
		}
		addOpLog("[领料] 删除");
		renderSuccess();
	}

	public void orderlist() {
		Long id = getParaToLong("id");
		setAttr("id", id);
		render("orderlist.html");
	}

	public void getOrderList() {
		List<Record> premddetail = Db.find("SELECT * FROM emd_order_detail WHERE emd_id = ?",
				new Object[] { getPara("id") });
		renderDatagrid(premddetail);
	}

	public void addOrderPage() {
		Long id = getParaToLong("emd_id");
		setAttr("emd_id", id);
		setAttr("model", Emd.me.findById(getPara("emd_id")));
		render("addOrder.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void addOrder() {
		EmdOrderDetail model = (EmdOrderDetail) getModel(EmdOrderDetail.class, "model");
		model.save();
		Emd pr = (Emd) Emd.me.findById(model.getLong("emd_id"));
		pr.set("order_date", model.getDate("order_time"));
		pr.update();
		addOpLog("[领料] 到货记录增加");
		renderSuccess();
	}

	public void updateOrderPage() {
		EmdOrderDetail emdor = (EmdOrderDetail) EmdOrderDetail.me.findById(getPara("id"));
		setAttr("model", emdor);
		Emd emd = (Emd) Emd.me.findById(emdor.getLong("emd_id"));
		setAttr("qty", emd.getInt("qty"));
		render("update.html");
		render("updateOrder.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void updateOrder() {
		logger.info("编辑到货记录开始....");
		EmdOrderDetail model = (EmdOrderDetail) EmdOrderDetail.me.findById(getPara("id"));
		model.set("order_time", getPara("model.order_time"));
		model.set("order_num", getPara("model.order_num"));
		model.set("order_remark", getPara("model.order_remark"));
		model.update();
		logger.info("开始更新采购表数据的到货时间id=" + getPara("id"));
		String sql = "update emd set order_date = (select max(order_time) from emd_order_detail where emd_id = ?) where id = ? ";
		Db.update(sql, new Object[] { model.getLong("emd_id"), model.getLong("emd_id") });
		logger.info("更新领料表数据的到货时间完成");
		logger.info("编辑到货记录结束....");
		addOpLog("[领料] 到货记录修改");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void orderDelete() {
		logger.info("删除到货记录开始....");
		Long[] ids = getParaValuesToLong("id[]");
		byte b;
		int i;
		Long[] arrayOfLong;
		for (i = (arrayOfLong = ids).length, b = 0; b < i;) {
			Long id = arrayOfLong[b];
			((EmdOrderDetail) (new EmdOrderDetail()).set("id", id)).delete();
			b++;
		}
		Long emd_id = getParaToLong("emd_id");
		String sql = "update emd set order_date = (select max(order_time) from emd_order_detail where emd_id = ?) where id = ? ";
		Db.update(sql, new Object[] { emd_id, emd_id });
		logger.info("更新领料表数据的到货时间完成");
		logger.info("删除到货记录结束....");
		addOpLog("[领料] 到货记录修改");
		renderSuccess();
	}

	public void detailPage() {
		Emd model = (Emd) Emd.me.findById(getPara("id"));
		setAttr("model", model);
		render("detail.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传领料信息文件...");
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
								if (valiflag)
									record.set(strings[1], v);
							}
							isUseRecord = isUseRecord ? valiflag : isUseRecord;
						}
						if (isUseRecord)
							batchRecord.add(record);
					}
					if (listResult.isEmpty()) {
						for (int i = 0; i < batchRecord.size(); i++)
							Db.save("emd", (Record) batchRecord.get(i));
						result.put("result", "success");
						result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
						logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传领料信息文件结束，共入库"
								+ batchRecord.size() + "条数据");
					} else {
						result.put("result", "fail");
						result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
						logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传领料信息文件异常结束");
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
		if (paginate2 != null) {
			String sql = "select sum(order_num) as order_total_num from emd_order_detail where emd_id = ?";
			for (Record record : paginate2) {
				Object object = Db.findFirst(sql, new Object[] { record.getLong("id") }).get("order_total_num");
				if (object == null)
					object = Integer.valueOf(0);
				record.set("order_total_num", object);
			}
		}
		String fileName = "领料表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
		String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
		File file = new File(path);
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("领料表");
		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("日期");
		row0.createCell(1).setCellValue("领料单编号");
		row0.createCell(2).setCellValue("工程号");
		row0.createCell(3).setCellValue("期望到货");
		row0.createCell(4).setCellValue("申请人");
		row0.createCell(5).setCellValue("能力中心");
		row0.createCell(6).setCellValue("实验室");
		row0.createCell(7).setCellValue("物料编号");
		row0.createCell(8).setCellValue("物料名称");
		row0.createCell(9).setCellValue("数量");
		row0.createCell(10).setCellValue("单价");
		row0.createCell(11).setCellValue("总价");
		row0.createCell(12).setCellValue("项目");
		row0.createCell(13).setCellValue("项目编号");
		row0.createCell(14).setCellValue("成本中心");
		row0.createCell(15).setCellValue("领料单状态");
		row0.createCell(16).setCellValue("实际领到日期");
		row0.createCell(17).setCellValue("制单&跟踪人");
		row0.createCell(18).setCellValue("备注");
		row0.createCell(19).setCellValue("已到货数量");
		for (int i = 0; i < paginate2.size(); i++) {
			Record record2 = (Record) paginate2.get(i);
			HSSFRow rowi = sheet.createRow(i + 1);
			rowi.createCell(0).setCellValue(isNull(record2.get("emd_date")));
			rowi.createCell(1).setCellValue(isNull(record2.get("emd_no")));
			rowi.createCell(2).setCellValue(isNull(record2.get("po_no")));
			rowi.createCell(3).setCellValue(isNull(record2.get("deadline")));
			rowi.createCell(4).setCellValue(isNull(record2.get("proposer")));
			rowi.createCell(5).setCellValue(isNull(record2.get("cc")));
			rowi.createCell(6).setCellValue(isNull(record2.get("lab")));
			rowi.createCell(7).setCellValue(isNull(record2.get("instrument_code")));
			rowi.createCell(8).setCellValue(isNull(record2.get("instrument_name")));
			rowi.createCell(9).setCellValue(isNull(record2.get("qty")));
			rowi.createCell(10).setCellValue(isNull(record2.get("unit_price")));
			rowi.createCell(11).setCellValue(isNull(record2.get("total")));
			rowi.createCell(12).setCellValue(isNull(record2.get("project")));
			rowi.createCell(13).setCellValue(isNull(record2.get("project_no")));
			rowi.createCell(14).setCellValue(isNull(record2.get("cost_center")));
			rowi.createCell(15).setCellValue(isNull(record2.get("emd_stauts")));
			rowi.createCell(16).setCellValue(isNull(record2.get("real_date")));
			rowi.createCell(17).setCellValue(isNull(record2.get("maker")));
			rowi.createCell(18).setCellValue(isNull(record2.get("remark")));
			rowi.createCell(19).setCellValue(isNull(record2.get("order_total_num")));
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
