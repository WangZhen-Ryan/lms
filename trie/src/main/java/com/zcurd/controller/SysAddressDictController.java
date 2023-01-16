package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.JsonRender;
import com.jfinal.upload.UploadFile;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.Check;
import com.zcurd.common.util.StringUtil;
import com.zcurd.common.util.ToolDateTime;
import com.zcurd.excel.ExcelUtils;
import com.zcurd.excel.utils.Utils;
import com.zcurd.model.SysAddressDict;
import java.io.BufferedInputStream;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class SysAddressDictController extends BaseController {
  private HSSFWorkbook wb;
  
  public void listPage() { render("list.html"); }
  
  public void listData() {
    Object[] queryParams = getQueryParams();
    String[] properties = (String[])queryParams[0];
    String[] symbols = (String[])queryParams[1];
    Object[] values = (Object[])queryParams[2];
    String orderBy = getOrderBy();
    if (StringUtil.isEmpty(orderBy))
      orderBy = "id desc"; 
    List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_address_dict", properties, symbols, values, orderBy, getPager());
    renderDatagrid(list, DBTool.countByMultPropertiesDbSource("zcurd_base", "sys_address_dict", properties, symbols, values));
  }
  
  public void addPage() { render("add.html"); }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void add() {
    SysAddressDict model = (SysAddressDict)getModel(SysAddressDict.class, "model");
    model.set("auto_val", String.valueOf(model.getStr("build_code")) + model.getStr("class_code") + model.getStr("rack_info"));
    model.save();
    addOpLog("[地址字典] 增加");
    renderSuccess();
  }
  
  public void updatePage() {
    setAttr("model", SysAddressDict.me.findById(getPara("id")));
    render("update.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void update() {
    SysAddressDict model = (SysAddressDict)SysAddressDict.me.findById(getPara("id"));
    model.set("build_code", getPara("model.build_code"));
    model.set("class_code", getPara("model.class_code"));
    model.set("rack_info", getPara("model.rack_info"));
    model.set("auto_val", String.valueOf(getPara("model.build_code")) + getPara("model.class_code") + getPara("model.rack_info"));
    model.update();
    addOpLog("[地址字典] 修改");
    renderSuccess();
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void delete() {
    Long[] ids = getParaValuesToLong("id[]");
    byte b; int i; Long[] arrayOfLong;
    for (i = (arrayOfLong = ids).length, b = 0; b < i; ) { Long id = arrayOfLong[b];
      ((SysAddressDict)((SysAddressDict)(new SysAddressDict()).set("id", id)).set("status", "0")).update(); b++; } 
    addOpLog("[地址字典] 删除");
    renderSuccess();
  }
  
  public void detailPage() {
    SysAddressDict model = (SysAddressDict)SysAddressDict.me.findById(getParaToLong("id"));
    setAttr("model", model);
    render("detail.html");
  }
  
  public void importExcel() {
    logger.info(String.valueOf(getSessionUser().getStr("user_name")) + "开始上传地址字典信息文件...");
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
          ruleMap.put(Integer.valueOf(i), (String[])((List)ruleExcelListList.get(i)).toArray(new String[0])); 
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
            List<String> list = (List)readExcel2List.get(i);
            record = new Record();
            for (int j = 0; j < list.size(); j++) {
              boolean valiflag = true;
              String val = (String)list.get(j);
              String[] strings = (String[])ruleMap.get(Integer.valueOf(j));
              if ("required:true".equals(strings[3]) && 
                StringUtils.isEmpty(val)) {
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
                      valiflag = (((Integer)v).intValue() >= Integer.parseInt(split[1]));
                      if (!valiflag) {
                        listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列最小值为" + 
                            Integer.parseInt(split[1]));
                      } else if (split.length == 3) {
                        valiflag = (((Integer)v).intValue() <= Integer.parseInt(split[2]));
                        if (!valiflag)
                          listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列最大值为" + 
                              Integer.parseInt(split[2])); 
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
            if (isUseRecord) {
              record.set("status", "1");
              record.set("auto_val", String.valueOf(record.getStr("build_code")) + record.getStr("class_code") + 
                  record.getStr("rack_info"));
              batchRecord.add(record);
            } 
          } 
          if (listResult.isEmpty()) {
            for (int i = 0; i < batchRecord.size(); i++)
              Db.save("sys_address_dict", (Record)batchRecord.get(i)); 
            result.put("result", "success");
            result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
            logger.info(
                String.valueOf(getSessionUser().getStr("user_name")) + "上传地址字典信息文件结束，共入库" + batchRecord.size() + "条数据");
          } else {
            result.put("result", "fail");
            result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
            logger.info(String.valueOf(getSessionUser().getStr("user_name")) + "上传地址字典文件异常结束");
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
      try {
        if (fis != null)
          fis.close(); 
        if (bis != null)
          bis.close(); 
        if (fis1 != null)
          fis1.close(); 
        if (bis1 != null)
          fis1.close(); 
      } catch (Exception exception) {}
    } 
    render((new JsonRender(result)).forIE());
  }
  
  public void expExcel() {
    String sqlExceptSelect = "select * from sys_address_dict a where status= '1' ";
    String auto_val = getPara("auto_val");
    if (StringUtils.isNotEmpty(auto_val))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and auto_val like '%" + auto_val + "%'"; 
    String orderby = "";
    String sort = getPara("sort");
    String order = getPara("order");
    if (!Check.IsStringNULL(sort) && !Check.IsStringNULL(order)) {
      List<String> sorts = Arrays.asList(sort.split(","));
      List<String> orders = Arrays.asList(order.split(","));
      if (sorts.size() > 0 && orders.size() > 0 && sorts.size() == sorts.size())
        for (int i = 0; i < sorts.size(); i++)
          orderby = String.valueOf(orderby) + (String)sorts.get(i) + " " + (String)orders.get(i) + ",";  
    } 
    sqlExceptSelect = String.valueOf(sqlExceptSelect) + "order by " + orderby + " a.id desc";
    String select = "select a.* ";
    List<Record> paginate2 = Db.find(sqlExceptSelect);
    String fileName = "地址字典表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
    String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
    System.out.println(path);
    File file = new File(path);
    this.wb = new HSSFWorkbook();
    HSSFSheet sheet = this.wb.createSheet("地址字典表");
    HSSFRow row0 = sheet.createRow(0);
    row0.createCell(0).setCellValue("楼栋号");
    row0.createCell(1).setCellValue("房间号");
    row0.createCell(2).setCellValue("位置信息");
    row0.createCell(3).setCellValue("地址编码");
    for (int i = 0; i < paginate2.size(); i++) {
      Record record2 = (Record)paginate2.get(i);
      HSSFRow rowi = sheet.createRow(i + 1);
      rowi.createCell(0).setCellValue((record2.get("build_code") == null) ? "" : record2.get("build_code").toString());
      rowi.createCell(1).setCellValue((record2.get("class_code") == null) ? "" : record2.get("class_code").toString());
      rowi.createCell(2).setCellValue((record2.get("rack_info") == null) ? "" : record2.get("rack_info").toString());
      rowi.createCell(3).setCellValue((record2.get("auto_val") == null) ? "" : record2.get("auto_val").toString());
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
