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
import com.zcurd.model.Material;
import com.zcurd.model.Pr;
import com.zcurd.model.PrOrderDetail;
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

public class PrController extends BaseController {
	private HSSFWorkbook wb;

	public void listPage() {
		render("list.html");
	}

	public void listData() {
		String select = "select a.* ";
		Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
		if (paginate.getList() != null) {
			String sql = "select sum(order_num) as order_total_num from pr_order_detail where pr_id = ?";
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
		String sqlSelect = getSearchText("pr");
		String sqlExceptSelect = " from pr a where 1= 1 ";
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
		Pr model = (Pr) getModel(Pr.class, "model");
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("requester", dictDataUser, model);
		CommonUtils.setFieldValue("buyer", dictDataUser, model);
		model.save();
		addOpLog("[采购] 增加");
		renderSuccess();
	}

	public void updatePage() {
		setAttr("dictDataUser", Material.me.getDictDataUser());
		setAttr("model", Pr.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		Pr model = (Pr) Pr.me.findById(getPara("id"));
		model.set("pr_date", getPara("model.pr_date"));
		model.set("pr_no", getPara("model.pr_no"));
		model.set("po_no", getPara("model.po_no"));
		model.set("deadline", getPara("model.deadline"));
		model.set("requester", getPara("model.requester"));
		model.set("project", getPara("model.project"));
		model.set("cc", getPara("model.cc"));
		model.set("lab", getPara("model.lab"));
		model.set("description", getPara("model.description"));
		model.set("qty", getPara("model.qty"));
		model.set("currency", getPara("model.currency"));
		model.set("unit_price", getPara("model.unit_price"));
		model.set("total", getPara("model.total"));
		model.set("buyer", getPara("model.buyer"));
		model.set("order_date", getPara("model.order_date"));
		model.set("supplier", getPara("model.supplier"));
		model.set("remark", getPara("model.remark"));
		Map<String, Object> dictDataUser = Material.me.getDictDataUser();
		CommonUtils.setFieldValue("requester", dictDataUser, model);
		CommonUtils.setFieldValue("buyer", dictDataUser, model);
		model.update();
		addOpLog("[采购] 修改");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void delete() {
		Long[] ids = getParaValuesToLong("id[]");
		String[] paraValues = getParaValues("id[]");
		byte b;
		int i;
		Long[] arrayOfLong;
		for (i = (arrayOfLong = ids).length, b = 0; b < i;) {
			Long id = arrayOfLong[b];
			((Pr) (new Pr()).set("id", id)).delete();
			b++;
		}
		if (paraValues != null && paraValues.length > 0)
			Db.update("delete from pr_order_detail where pr_id in(" + StringUtils.join(paraValues, ",") + ")");
		addOpLog("[采购] 删除");
		renderSuccess();
	}

	public void orderlist() {
		Long id = getParaToLong("id");
		setAttr("id", id);
		render("orderlist.html");
	}

	public void getOrderList() {
		List<Record> premddetail = Db.find("SELECT * FROM pr_order_detail WHERE pr_id = ?",
				new Object[] { getPara("id") });
		renderDatagrid(premddetail);
	}

	public void addOrderPage() {
		Long id = getParaToLong("pr_id");
		setAttr("pr_id", id);
		setAttr("model", Pr.me.findById(getPara("pr_id")));
		render("addOrder.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void addOrder() {
		PrOrderDetail model = (PrOrderDetail) getModel(PrOrderDetail.class, "model");
		model.save();
		Pr pr = (Pr) Pr.me.findById(model.getLong("pr_id"));
		pr.set("order_date", model.getDate("order_time"));
		pr.update();
		addOpLog("[采购] 到货记录增加");
		renderSuccess();
	}

	public void updateOrderPage() {
		PrOrderDetail pd = (PrOrderDetail) PrOrderDetail.me.findById(getPara("id"));
		Pr pr = (Pr) Pr.me.findById(pd.getLong("pr_id"));
		setAttr("qty", pr.getInt("qty"));
		setAttr("model", pd);
		render("update.html");
		render("updateOrder.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void updateOrder() {
		logger.info("编辑到货记录开始....");
		PrOrderDetail model = (PrOrderDetail) PrOrderDetail.me.findById(getPara("id"));
		model.set("order_time", getPara("model.order_time"));
		model.set("order_num", getPara("model.order_num"));
		model.set("order_remark", getPara("model.order_remark"));
		model.update();
		logger.info("开始更新采购表数据的到货时间id=" + getPara("id"));
		String sql = "update pr set order_date = (select max(order_time) from pr_order_detail where pr_id = ?) where id = ? ";
		Db.update(sql, new Object[] { model.getLong("pr_id"), model.getLong("pr_id") });
		logger.info("更新采购表数据的到货时间完成");
		logger.info("编辑到货记录结束....");
		addOpLog("[采购] 到货记录修改");
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
			((PrOrderDetail) (new PrOrderDetail()).set("id", id)).delete();
			b++;
		}
		Long pr_id = getParaToLong("pr_id");
		String sql = "update pr set order_date = (select max(order_time) from pr_order_detail where pr_id = ?) where id = ? ";
		Db.update(sql, new Object[] { pr_id, pr_id });
		logger.info("更新采购表数据的到货时间完成");
		logger.info("删除到货记录结束....");
		addOpLog("[采购] 到货记录修改");
		renderSuccess();
	}

	public void detailPage() {
		Pr model = (Pr) Pr.me.findById(getPara("id"));
		setAttr("model", model);
		render("detail.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传采购信息文件...");
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
							Db.save("pr", (Record) batchRecord.get(i));
						result.put("result", "success");
						result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
						logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传采购信息文件结束，共入库"
								+ batchRecord.size() + "条数据");
					} else {
						result.put("result", "fail");
						result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
						logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传采购信息文件异常结束");
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
			String sql = "select sum(order_num) as order_total_num from pr_order_detail where pr_id = ?";
			for (Record record : paginate2) {
				Object object = Db.findFirst(sql, new Object[] { record.getLong("id") }).get("order_total_num");
				if (object == null)
					object = Integer.valueOf(0);
				record.set("order_total_num", object);
			}
		}
		String fileName = "采购表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
		String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
		File file = new File(path);
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("采购表");
		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("填写日");
		row0.createCell(1).setCellValue("PR号");
		row0.createCell(2).setCellValue("PO号");
		row0.createCell(3).setCellValue("期望到货");
		row0.createCell(4).setCellValue("需求人");
		row0.createCell(5).setCellValue("项目");
		row0.createCell(6).setCellValue("能力中心");
		row0.createCell(7).setCellValue("实验室");
		row0.createCell(8).setCellValue("描述");
		row0.createCell(9).setCellValue("数量");
		row0.createCell(10).setCellValue("货币");
		row0.createCell(11).setCellValue("单价");
		row0.createCell(12).setCellValue("总价");
		row0.createCell(13).setCellValue("填写人");
		row0.createCell(14).setCellValue("到货日期");
		row0.createCell(15).setCellValue("供应商");
		row0.createCell(16).setCellValue("备注");
		row0.createCell(17).setCellValue("已到货数量");
		for (int i = 0; i < paginate2.size(); i++) {
			Record record2 = (Record) paginate2.get(i);
			HSSFRow rowi = sheet.createRow(i + 1);
			rowi.createCell(0).setCellValue(isNull(record2.get("pr_date")));
			rowi.createCell(1).setCellValue(isNull(record2.get("pr_no")));
			rowi.createCell(2).setCellValue(isNull(record2.get("po_no")));
			rowi.createCell(3).setCellValue(isNull(record2.get("deadline")));
			rowi.createCell(4).setCellValue(isNull(record2.get("requester")));
			rowi.createCell(5).setCellValue(isNull(record2.get("project")));
			rowi.createCell(6).setCellValue(isNull(record2.get("cc")));
			rowi.createCell(7).setCellValue(isNull(record2.get("lab")));
			rowi.createCell(8).setCellValue(isNull(record2.get("description")));
			rowi.createCell(9).setCellValue(isNull(record2.get("qty")));
			rowi.createCell(10).setCellValue(isNull(record2.get("currency")));
			rowi.createCell(11).setCellValue(isNull(record2.get("unit_price")));
			rowi.createCell(12).setCellValue(isNull(record2.get("total")));
			rowi.createCell(13).setCellValue(isNull(record2.get("buyer")));
			rowi.createCell(14).setCellValue(isNull(record2.get("order_date")));
			rowi.createCell(15).setCellValue(isNull(record2.get("supplier")));
			rowi.createCell(16).setCellValue(isNull(record2.get("remark")));
			rowi.createCell(17).setCellValue(isNull(record2.get("order_total_num")));
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
