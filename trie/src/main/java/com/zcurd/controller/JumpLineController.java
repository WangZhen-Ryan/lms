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
import com.zcurd.ext.render.csv.CsvRender;
import com.zcurd.model.JumpLine;
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

public class JumpLineController extends BaseController {
  private HSSFWorkbook wb;
  
  public void listPage() {
    setAttr("type", "0");
    render("list.html");
  }
  
  public void list1Page() {
    setAttr("type", "1");
    render("list1.html");
  }
  
  public void list2Page() {
    setAttr("type", "2");
    render("list2.html");
  }
  
  public void listData() {
    String select = "select a.* ";
    Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
    Map<String, Object> dictDatalocation = JumpLine.me.getDictDatalocation();
    Map<String, Object> dictDatatype = JumpLine.me.getDictDatatype();
    for (Record record : paginate.getList()) {
      String fieldName = "location";
      if (record.get(fieldName) != null && dictDatalocation.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatalocation.get(record.get(fieldName).toString())); 
      fieldName = "type";
      if (record.get(fieldName) != null && dictDatatype.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatatype.get(record.get(fieldName).toString())); 
    } 
    renderDatagrid(paginate);
  }
  
  private String sqlExceptSelect() {
    String sqlSelect = getSearchText("jump_line");
    String sqlExceptSelect = " from jump_line a,sys_address_dict b where a.location  = b.id ";
    String location = getPara("location");
    String type = getPara("type");
    if (StringUtils.isNotEmpty(location))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'"; 
    if (StringUtils.isNotEmpty(sqlSelect))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and (" + sqlSelect + ")"; 
    if (StringUtils.isNotEmpty(type))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.type= '" + type + "'"; 
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
    return String.valueOf(sqlExceptSelect) + "order by " + orderby + " a.id desc";
  }
  
  public void addPage() {
    setAttr("dictDatalocation", JumpLine.me.getDictDatalocation());
    setAttr("dictDatatype", JumpLine.me.getDictDatatype());
    setAttr("dictDataUser", Material.me.getDictDataUser());
    setAttr("type", getPara("type"));
    render("add.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void add() {
    JumpLine model = (JumpLine)getModel(JumpLine.class, "model");
    Map<String, Object> dictDataUser = Material.me.getDictDataUser();
    CommonUtils.setFieldValue("user", dictDataUser, model);
    model.save();
    addOpLog("[跳线表] 增加");
    renderSuccess();
  }
  
  public void updatePage() {
    setAttr("dictDatalocation", JumpLine.me.getDictDatalocation());
    setAttr("dictDatatype", JumpLine.me.getDictDatatype());
    setAttr("dictDataUser", Material.me.getDictDataUser());
    setAttr("model", JumpLine.me.findById(getPara("id")));
    render("update.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void update() {
    JumpLine model = (JumpLine)JumpLine.me.findById(getPara("id"));
    Record record = new Record();
    record.setColumns(model);
    record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "0").remove("id");
    Db.save("jump_line_log", record);
    model.set("location", getPara("model.location"));
    model.set("con_cabinet", getPara("model.con_cabinet"));
    model.set("dis_no", getPara("model.dis_no"));
    model.set("corr_pos", getPara("model.corr_pos"));
    model.set("jump_conn", getPara("model.jump_conn"));
    model.set("user", getPara("model.user"));
    model.set("from_date", getPara("model.from_date"));
    model.set("till_date", getPara("model.till_date"));
    model.set("project", getPara("model.project"));
    model.set("remark", getPara("model.remark"));
    Map<String, Object> dictDataUser = Material.me.getDictDataUser();
    CommonUtils.setFieldValue("user", dictDataUser, model);
    model.update();
    addOpLog("[跳线表] 修改");
    renderSuccess();
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void delete() {
    Long[] ids = getParaValuesToLong("id[]");
    byte b; int i; Long[] arrayOfLong;
    for (i = (arrayOfLong = ids).length, b = 0; b < i; ) { Long id = arrayOfLong[b];
      JumpLine model = (JumpLine)JumpLine.me.findById(id);
      ((JumpLine)(new JumpLine()).set("id", id)).delete();
      
      Record record = new Record();
      record.setColumns((Model)model);
      record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "1").remove("id");
      Db.save("jump_line_log", record);
      b++; }
    addOpLog("[跳线表] 删除");
    renderSuccess();
  }
  
  public void detailPage() {
    JumpLine model = (JumpLine)JumpLine.me.findById(getParaToLong("id"));
    Map<String, Object> dictDatalocation = JumpLine.me.getDictDatalocation();
    if (model.get("location") != null && dictDatalocation.get(model.get("location").toString()) != null)
      model.set("location", dictDatalocation.get(model.get("location").toString())); 
    Map<String, Object> dictDatatype = JumpLine.me.getDictDatatype();
    if (model.get("type") != null && dictDatatype.get(model.get("type").toString()) != null)
      model.set("type", dictDatatype.get(model.get("type").toString())); 
    setAttr("model", model);
    render("detail.html");
  }
  
  public void exportCsv() {
    String select = "select a.* ";
    String sqlExceptSelect = " from jump_line a,sys_address_dict b where a.location  = b.id ";
    String location = getPara("location");
    if (StringUtils.isNotEmpty(location))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'"; 
    sqlExceptSelect = String.valueOf(sqlExceptSelect) + "order by a.id desc";
    List<Record> list = Db.find(String.valueOf(select) + sqlExceptSelect);
    Map<String, Object> dictDatalocation = JumpLine.me.getDictDatalocation();
    Map<String, Object> dictDatatype = JumpLine.me.getDictDatatype();
    for (Record record : list) {
      String fieldName = "location";
      if (dictDatalocation.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatalocation.get(record.get(fieldName).toString())); 
      fieldName = "type";
      if (dictDatatype.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatatype.get(record.get(fieldName).toString())); 
    } 
    List<String> headers = new ArrayList<String>();
    List<String> clomuns = new ArrayList<String>();
    headers.add("位置");
    clomuns.add("location");
    headers.add("汇聚柜");
    clomuns.add("con_cabinet");
    headers.add("配线架号");
    clomuns.add("dis_no");
    headers.add("对应位");
    clomuns.add("corr_pos");
    headers.add("跳线连接");
    clomuns.add("jump_conn");
    headers.add("使用人");
    clomuns.add("user");
    headers.add("起始时间");
    clomuns.add("from_date");
    headers.add("结束时间");
    clomuns.add("till_date");
    headers.add("项目");
    clomuns.add("project");
    headers.add("备注");
    clomuns.add("remark");
    CsvRender csvRender = new CsvRender(headers, list);
    csvRender.clomuns(clomuns);
    csvRender.fileName("跳线表");
    addOpLog("[跳线表] 导出cvs");
    render(csvRender);
  }
  
  public void importExcelPage() {
    setAttr("type", getPara("type"));
    super.importExcelPage();
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void importExcel() {
    String para = getPara("type");
    String msg = "";
    if ("0".equals(para)) {
      msg = "数据";
    } else if ("1".equals(para)) {
      msg = "光纤";
    } else if ("2".equals(para)) {
      msg = "语音";
    } 
    logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传" + msg + "信息文件...");
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
          List<Record> find = Db.find("select id as k ,auto_val as v from sys_address_dict  where `status`='1'");
          if (find == null || find.isEmpty()) {
            result.put("result", "fail");
            result.put("msg", "请管理员维护地址字典菜单中地址编码信息！");
          } else {
            Map<String, Long> rackAddressMap = new HashMap<String, Long>();
            for (int i = 0; i < find.size(); i++) {
              Record record = (Record)find.get(i);
              rackAddressMap.put(record.getStr("v"), record.getLong("k"));
            } 
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
                            listResult.add("Excel" + (i + 2) + "行" + strings[0] + 
                                "列最大值为" + Integer.parseInt(split[2])); 
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
                    } 
                    record.set(strings[1], v);
                  } 
                } 
                isUseRecord = isUseRecord ? valiflag : isUseRecord;
              } 
              if (isUseRecord) {
                record.set("type", para);
                batchRecord.add(record);
              } 
            } 
            if (listResult.isEmpty()) {
              for (int i = 0; i < batchRecord.size(); i++)
                Db.save("jump_line", (Record)batchRecord.get(i)); 
              result.put("result", "success");
              result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
              logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传" + msg + "信息文件结束，共入库" + 
                  batchRecord.size() + "条数据");
            } else {
              result.put("result", "fail");
              result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
              logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传" + msg + "信息文件异常结束");
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
    Map<String, Object> dictDatalocation = JumpLine.me.getDictDatalocation();
    Map<String, Object> dictDatatype = JumpLine.me.getDictDatatype();
    for (Record record : paginate2) {
      String fieldName = "location";
      if (record.get(fieldName) != null && dictDatalocation.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatalocation.get(record.get(fieldName).toString())); 
      fieldName = "type";
      if (record.get(fieldName) != null && dictDatatype.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatatype.get(record.get(fieldName).toString())); 
    } 
    String type = getPara("type");
    String typeName = "Sheet1";
    if (type != null)
      if (type.equals("0")) {
        typeName = "数据";
      } else if (type.equals("1")) {
        typeName = "光纤";
      } else if (type.equals("2")) {
        typeName = "语音";
      }  
    String fileName = "跳线管理-" + typeName + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
    String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
    File file = new File(path);
    this.wb = new HSSFWorkbook();
    HSSFSheet sheet = this.wb.createSheet(typeName);
    HSSFRow row0 = sheet.createRow(0);
    row0.createCell(0).setCellValue("位置");
    row0.createCell(1).setCellValue("汇聚柜");
    row0.createCell(2).setCellValue("配线架号");
    row0.createCell(3).setCellValue("对应位");
    row0.createCell(4).setCellValue("跳线连接");
    row0.createCell(5).setCellValue("使用人");
    row0.createCell(6).setCellValue("起始时间");
    row0.createCell(7).setCellValue("结束时间");
    row0.createCell(8).setCellValue("项目");
    row0.createCell(9).setCellValue("备注");
    for (int i = 0; i < paginate2.size(); i++) {
      Record record2 = (Record)paginate2.get(i);
      HSSFRow rowi = sheet.createRow(i + 1);
      rowi.createCell(0).setCellValue(isNull(record2.get("location")));
      rowi.createCell(1).setCellValue(isNull(record2.get("con_cabinet")));
      rowi.createCell(2).setCellValue(isNull(record2.get("dis_no")));
      rowi.createCell(3).setCellValue(isNull(record2.get("corr_pos")));
      rowi.createCell(4).setCellValue(isNull(record2.get("jump_conn")));
      rowi.createCell(5).setCellValue(isNull(record2.get("user")));
      rowi.createCell(6).setCellValue(isNull(record2.get("from_date")));
      rowi.createCell(7).setCellValue(isNull(record2.get("till_date")));
      rowi.createCell(8).setCellValue(isNull(record2.get("project")));
      rowi.createCell(9).setCellValue(isNull(record2.get("remark")));
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
