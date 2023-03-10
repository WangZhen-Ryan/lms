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
    addOpLog("[????????????] ??????");
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
    addOpLog("[????????????] ??????");
    renderSuccess();
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void delete() {
    Long[] ids = getParaValuesToLong("id[]");
    byte b; int i; Long[] arrayOfLong;
    for (i = (arrayOfLong = ids).length, b = 0; b < i; ) { Long id = arrayOfLong[b];
      ((SysAddressDict)((SysAddressDict)(new SysAddressDict()).set("id", id)).set("status", "0")).update(); b++; } 
    addOpLog("[????????????] ??????");
    renderSuccess();
  }
  
  public void detailPage() {
    SysAddressDict model = (SysAddressDict)SysAddressDict.me.findById(getParaToLong("id"));
    setAttr("model", model);
    render("detail.html");
  }
  
  public void importExcel() {
    logger.info(String.valueOf(getSessionUser().getStr("user_name")) + "????????????????????????????????????...");
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
          ruleMap.put(Integer.valueOf(i), (String[])((List)ruleExcelListList.get(i)).toArray(new String[0])); 
        fis1 = new FileInputStream(uploadFile);
        bis1 = new BufferedInputStream(fis1);
        List<List<String>> readExcel2List = ExcelUtils.getInstance().readExcel2List(bis1, 1);
        if (readExcel2List == null || readExcel2List.isEmpty()) {
          result.put("result", "fail");
          result.put("msg", "??????????????????????????????????????????");
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
                      valiflag = (((Integer)v).intValue() >= Integer.parseInt(split[1]));
                      if (!valiflag) {
                        listResult.add("Excel" + (i + 2) + "???" + strings[0] + "???????????????" + 
                            Integer.parseInt(split[1]));
                      } else if (split.length == 3) {
                        valiflag = (((Integer)v).intValue() <= Integer.parseInt(split[2]));
                        if (!valiflag)
                          listResult.add("Excel" + (i + 2) + "???" + strings[0] + "???????????????" + 
                              Integer.parseInt(split[2])); 
                      } 
                    } 
                  } catch (Exception e) {
                    valiflag = false;
                    listResult.add("Excel" + (i + 2) + "???" + strings[0] + "?????????????????????");
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
            result.put("msg", "????????????,?????????" + batchRecord.size() + "?????????");
            logger.info(
                String.valueOf(getSessionUser().getStr("user_name")) + "????????????????????????????????????????????????" + batchRecord.size() + "?????????");
          } else {
            result.put("result", "fail");
            result.put("msg", "????????????!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
            logger.info(String.valueOf(getSessionUser().getStr("user_name")) + "????????????????????????????????????");
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
    String fileName = "???????????????" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
    String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
    System.out.println(path);
    File file = new File(path);
    this.wb = new HSSFWorkbook();
    HSSFSheet sheet = this.wb.createSheet("???????????????");
    HSSFRow row0 = sheet.createRow(0);
    row0.createCell(0).setCellValue("?????????");
    row0.createCell(1).setCellValue("?????????");
    row0.createCell(2).setCellValue("????????????");
    row0.createCell(3).setCellValue("????????????");
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
