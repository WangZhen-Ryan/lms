package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.JsonRender;
import com.jfinal.upload.UploadFile;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.common.util.ToolDateTime;
import com.zcurd.excel.ExcelUtils;
import com.zcurd.excel.utils.Utils;
import com.zcurd.ext.render.csv.CsvRender;
import com.zcurd.model.SysDict;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class SysDictController extends BaseController {
  private HSSFWorkbook wb;
  
  public void listPage() {
    setAttr("dictDatadict_type", SysDict.me.getDictDatadict_type());
    render("list.html");
  }
  
  public void listData() {
    String select = "select a.* ";
    String sqlExceptSelect = sqlExceptSelect();
    String orderBy = getOrderBy();
    if (StringUtil.isEmpty(orderBy)) {
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by a.id desc";
    } else {
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by " + orderBy;
    } 
    Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect);
    Map<String, Object> dictDatadict_type = SysDict.me.getDictDatadict_type();
    for (Record record : paginate.getList()) {
      String fieldName = "dict_type";
      if (record.get(fieldName) != null && dictDatadict_type.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatadict_type.get(record.get(fieldName).toString())); 
    } 
    renderDatagrid(paginate);
  }
  
  private String sqlExceptSelect() {
    String sqlExceptSelect = " from sys_dict a where 1=1";
    String dict_type = getPara("dict_type");
    String dict_key = getPara("dict_key");
    String dict_value = getPara("dict_value");
    String _start_create_time = getPara("_start_create_time");
    String _end_create_time = getPara("_end_create_time");
    if (StringUtils.isNotEmpty(dict_type))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.dict_type = '" + dict_type + "'"; 
    if (StringUtils.isNotEmpty(dict_key))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.dict_key like '%" + dict_key + "%'"; 
    if (StringUtils.isNotEmpty(dict_value))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.dict_value like'%" + dict_value + "%'"; 
    if (StringUtils.isNotEmpty(_start_create_time))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.create_time >='" + _start_create_time + "'"; 
    if (StringUtils.isNotEmpty(_end_create_time))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.create_time >='" + _end_create_time + "'"; 
    return String.valueOf(sqlExceptSelect) + " and a.dict_type != 'LDAP设置'";
  }
  
  public void addPage() {
    setAttr("dictDatadict_type", SysDict.me.getDictDatadict_type());
    render("add.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void add() {
    SysDict sysDict = (SysDict)getModel(SysDict.class, "model");
    List<SysDict> find = SysDict.me.find("select id from sys_dict where dict_type=? and dict_key=?", new Object[] { sysDict.getStr("dict_type"), sysDict.getStr("dict_key") });
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
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void update() {
    SysDict model = (SysDict)SysDict.me.findById(getPara("id"));
    model.set("dict_value", getPara("model.dict_value"));
    model.update();
    addOpLog("[数据字典] 修改");
    renderSuccess();
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void delete() {
    Integer[] ids = getParaValuesToInt("id[]");
    byte b; int i; Integer[] arrayOfInteger;
    for (i = (arrayOfInteger = ids).length, b = 0; b < i; ) { Integer id = arrayOfInteger[b];
      ((SysDict)(new SysDict()).set("id", id)).delete();
      
      b++; }
    addOpLog("[数据字典] 删除");
    renderSuccess();
  }
  
  public void detailPage() {
    SysDict model = (SysDict)SysDict.me.findById(getParaToInt("id"));
    Map<String, Object> dictDatadict_type = SysDict.me.getDictDatadict_type();
    if (dictDatadict_type.get(model.get("dict_type").toString()) != null)
      model.set("dict_type", dictDatadict_type.get(model.get("dict_type").toString())); 
    setAttr("model", model);
    render("detail.html");
  }
  
  public void exportCsv() {
    Object[] queryParams = getQueryParams();
    String[] properties = (String[])queryParams[0];
    String[] symbols = (String[])queryParams[1];
    Object[] values = (Object[])queryParams[2];
    String orderBy = getOrderBy();
    if (StringUtil.isEmpty(orderBy))
      orderBy = "id desc"; 
    List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "sys_dict", properties, symbols, values);
    Map<String, Object> dictDatadict_type = SysDict.me.getDictDatadict_type();
    for (Record record : list) {
      String fieldName = "dict_type";
      if (dictDatadict_type.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatadict_type.get(record.get(fieldName).toString())); 
    } 
    List<String> headers = new ArrayList<String>();
    List<String> clomuns = new ArrayList<String>();
    headers.add("类型");
    clomuns.add("dict_type");
    headers.add("值");
    clomuns.add("dict_value");
    headers.add("键");
    clomuns.add("dict_key");
    CsvRender csvRender = new CsvRender(headers, list);
    csvRender.clomuns(clomuns);
    csvRender.fileName("数据字典");
    addOpLog("[数据字典] 导出cvs");
    render(csvRender);
  }
  
  public void importExcel() {
    logger.info(String.valueOf(getSessionUser().getStr("user_name")) + "开始上传数据字典信息文件...");
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
            if (isUseRecord)
              batchRecord.add(record); 
          } 
          if (listResult.isEmpty()) {
            Db.batchSave("sys_dict", batchRecord, 1000);
            result.put("result", "success");
            result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
            logger.info(String.valueOf(getSessionUser().getStr("user_name")) + "上传数据字典信息文件结束，共入库" + batchRecord.size() + "条数据");
          } else {
            result.put("result", "fail");
            result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
            logger.info(String.valueOf(getSessionUser().getStr("user_name")) + "上传数据字典文件异常结束");
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
    String select = "select a.* ";
    String sqlExceptSelect = sqlExceptSelect();
    String orderBy = getOrderBy();
    if (StringUtil.isEmpty(orderBy)) {
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by a.id desc";
    } else {
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by " + orderBy;
    } 
    List<Record> paginate2 = Db.find(String.valueOf(select) + sqlExceptSelect);
    Map<String, Object> dictDatadict_type = SysDict.me.getDictDatadict_type();
    for (Record record : paginate2) {
      String fieldName = "dict_type";
      if (record.get(fieldName) != null && dictDatadict_type.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatadict_type.get(record.get(fieldName).toString())); 
    } 
    String fileName = "数据字典表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
    String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
    System.out.println(path);
    File file = new File(path);
    this.wb = new HSSFWorkbook();
    HSSFSheet sheet = this.wb.createSheet("数据字典");
    HSSFRow row0 = sheet.createRow(0);
    row0.createCell(0).setCellValue("类型");
    row0.createCell(1).setCellValue("键");
    row0.createCell(2).setCellValue("值");
    row0.createCell(3).setCellValue("创建时间");
    for (int i = 0; i < paginate2.size(); i++) {
      Record record2 = (Record)paginate2.get(i);
      HSSFRow rowi = sheet.createRow(i + 1);
      rowi.createCell(0).setCellValue((record2.get("dict_type") == null) ? "" : record2.get("dict_type").toString());
      rowi.createCell(1).setCellValue((record2.get("dict_key") == null) ? "" : record2.get("dict_key").toString());
      rowi.createCell(2).setCellValue((record2.get("dict_value") == null) ? "" : record2.get("dict_value").toString());
      rowi.createCell(3).setCellValue(appointmentTime(record2.get("create_time")));
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
