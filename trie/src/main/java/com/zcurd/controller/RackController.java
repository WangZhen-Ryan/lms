package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;
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
import com.zcurd.model.Material;
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

public class RackController extends BaseController {
  private static final Log logger = Log.getLog(RackController.class);
  
  private HSSFWorkbook wb;
  
  public void listPage() {
    setAttr("dictDataposition", Rack.me.getDictDataposition());
    setAttr("dictDataStatus", Material.me.getDictDataStatus());
    render("list.html");
  }
  
  public void listData() {
    String select = "select a.* ";
    Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect());
    Map<String, Object> dictDatalocation = Rack.me.getDictDatalocation();
    Map<String, Object> dictDataposition = Rack.me.getDictDataposition();
    for (Record record : paginate.getList()) {
      String fieldName = "location";
      if (record.get(fieldName) != null && dictDatalocation.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatalocation.get(record.get(fieldName).toString())); 
      fieldName = "position";
      if (record.get(fieldName) != null && dictDataposition.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDataposition.get(record.get(fieldName).toString())); 
    } 
    renderDatagrid(paginate);
  }
  
  private String sqlExceptSelect() {
    String sqlSelect = getSearchText("rack");
    String sqlExceptSelect = " from rack a,sys_address_dict b where a.location  = b.id ";
    String position = getPara("position");
    String location = getPara("location");
    String project = getPara("project");
    String platform_name = getPara("platform_name");
    String Status = getPara("Status");
    String user = getPara("user");
    if (StringUtils.isNotEmpty(position))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.position = '" + position + "'"; 
    if (StringUtils.isNotEmpty(project))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.project like '%" + project + "%'"; 
    if (StringUtils.isNotEmpty(location))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'"; 
    if (StringUtils.isNotEmpty(platform_name))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.platform_name like '%" + platform_name + "%'"; 
    if (StringUtils.isNotEmpty(Status))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.status ='" + Status + "'"; 
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
          orderby = String.valueOf(orderby) + (String)sorts.get(i) + " " + (String)orders.get(i) + ",";  
    } 
    return String.valueOf(sqlExceptSelect) + "order by " + orderby + " a.id desc";
  }
  
  public void addPage() {
    setAttr("dictDatalocation", Rack.me.getDictDatalocation());
    setAttr("dictDataposition", Rack.me.getDictDataposition());
    setAttr("dictDataStatus", Material.me.getDictDataStatus());
    setAttr("dictDataUser", Material.me.getDictDataUser());
    render("add.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void add() {
    Rack model = (Rack)getModel(Rack.class, "model");
    Map<String, Object> dictDataUser = Material.me.getDictDataUser();
    CommonUtils.setFieldValue("user", dictDataUser, model);
    CommonUtils.setFieldValue("contact", dictDataUser, model);
    CommonUtils.setFieldValue("tpm", dictDataUser, model);
    CommonUtils.setFieldValue("pl", dictDataUser, model);
    model.save();
    addOpLog("[????????????] ??????");
    renderSuccess();
  }
  
  public void updatePage() {
    setAttr("dictDatalocation", Rack.me.getDictDatalocation());
    setAttr("dictDataposition", Rack.me.getDictDataposition());
    setAttr("dictDataStatus", Material.me.getDictDataStatus());
    setAttr("dictDataUser", Material.me.getDictDataUser());
    setAttr("model", Rack.me.findById(getPara("id")));
    render("update.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void update() {
    Rack model = (Rack)Rack.me.findById(getPara("id"));
    Record record = new Record();
    record.setColumns(model);
    record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "0").remove("id");
    Db.save("rack_log", record);
    model.set("location", getParaToLong("model.location"));
    model.set("position", getPara("model.position"));
    model.set("type", getPara("model.type"));
    model.set("sn", getPara("model.sn"));
    model.set("mac_address", getPara("model.mac_address"));
    model.set("port", getPara("model.port"));
    model.set("ip_address", getPara("model.ip_address"));
    model.set("sub_mark", getPara("model.sub_mark"));
    model.set("gate_address", getPara("model.gate_address"));
    model.set("user", getPara("model.user"));
    model.set("Status", getPara("model.Status"));
    model.set("platform_name", getPara("model.platform_name"));
    model.set("nt_info", getPara("model.nt_info"));
    model.set("lt_info", getPara("model.lt_info"));
    model.set("olt_ip", getPara("model.olt_ip"));
    model.set("port_server_info", getPara("model.port_server_info"));
    model.set("port_serve_link", getPara("model.port_serve_link"));
    model.set("stc_vstc_info", getPara("model.stc_vstc_info"));
    model.set("stc_nt_link", getPara("model.stc_nt_link"));
    model.set("lt_ont_link", getPara("model.lt_ont_link"));
    model.set("ont_switch_link", getPara("model.ont_switch_link"));
    model.set("switch_stc_link", getPara("model.switch_stc_link"));
    model.set("pcta_info", getPara("model.pcta_info"));
    model.set("pl_code", getPara("model.pl_code"));
    model.set("from_date", getPara("model.from_date"));
    model.set("till_date", getPara("model.till_date"));
    model.set("project", getPara("model.project"));
    model.set("sub_project", getPara("model.sub_project"));
    model.set("cc", getPara("model.cc"));
    model.set("tpm", getPara("model.tpm"));
    model.set("pl", getPara("model.pl"));
    model.set("remark", getPara("model.remark"));
    model.set("contact", getPara("model.contact"));
    Map<String, Object> dictDataUser = Material.me.getDictDataUser();
    CommonUtils.setFieldValue("user", dictDataUser, model);
    CommonUtils.setFieldValue("contact", dictDataUser, model);
    CommonUtils.setFieldValue("tpm", dictDataUser, model);
    CommonUtils.setFieldValue("pl", dictDataUser, model);
    model.update();
    addOpLog("[????????????] ??????");
    renderSuccess();
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void delete() {
    Long[] ids = getParaValuesToLong("id[]");
    Record record = null;
    byte b; int i; Long[] arrayOfLong;
    for (i = (arrayOfLong = ids).length, b = 0; b < i; ) { Long id = arrayOfLong[b];
      Rack model = (Rack)Rack.me.findById(id);
      ((Rack)(new Rack()).set("id", id)).delete();
      
      record = new Record();
      record.setColumns((Model)model);
      record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "1").remove("id");
      Db.save("rack_log", record); b++; }
    addOpLog("[????????????] ??????");
    renderSuccess();
  }
  
  public void detailPage() {
    Rack model = (Rack)Rack.me.findById(getParaToLong("id"));
    Map<String, Object> dictDatalocation = Rack.me.getDictDatalocation();
    if (dictDatalocation.get(model.get("location").toString()) != null)
      model.set("location", dictDatalocation.get(model.get("location").toString())); 
    Map<String, Object> dictDataposition = Rack.me.getDictDataposition();
    if (model.get("position") != null && dictDataposition.get(model.get("position").toString()) != null)
      model.set("position", dictDataposition.get(model.get("position").toString())); 
    setAttr("model", model);
    render("detail.html");
  }
  
  public void picpage() {
    Rack model = (Rack)Rack.me.findById(getPara("id"));
    Map<String, Object> dictDatalocation = Rack.me.getDictDatalocation();
    Map<String, Object> dictDataposition = Rack.me.getDictDataposition();
    CommonUtils.setFieldValue("location", dictDatalocation, model);
    CommonUtils.setFieldValue("position", dictDataposition, model);
    setAttr("model", model);
    render("pic.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void importpic() {
    String id = getPara("id");
    logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "????????????????????????...");
    Map<String, Object> result = new HashMap<String, Object>();
    String baPath = String.valueOf(PathKit.getWebRootPath()) + File.separator + "res" + File.separator + "upload" + File.separator + 
      "pic" + File.separator + "rack";
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
    Rack model = (Rack)Rack.me.findById(id);
    Record record = new Record();
    record.setColumns(model);
    record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "5")
      .set("update_time", new Timestamp(System.currentTimeMillis())).remove("id");
    Db.save("rack_log", record);
    model.set("pic", String.valueOf(id) + "." + suffix);
    model.update();
    addOpLog("[rack]????????????!");
    result.put("result", "success");
    result.put("msg", "????????????????????????!");
    render((new JsonRender(result)).forIE());
  }
  
  public void appointmentPage() {
    setAttr("idlist", getPara("idlist"));
    render("appointment.html");
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void appointment() {
    String[] idlist = getPara("idlist").split(",");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    Record record = null;
    String mailText = "???????????????????????????????????????<br>";
    for (int i = 0; i < idlist.length; i++) {
      String id = idlist[i];
      Rack model = (Rack)Rack.me.findById(id);
      record = new Record();
      record.setColumns(model);
      record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "7")
        .set("update_time", timestamp).remove("id");
      Db.save("rack_log", record);
      model.set("appointment_id", getSessionUser().getStr("id"));
      model.set("appointment_name", getSessionUser().getStr("display_name"));
      model.set("appointment_start", getPara("model.appointment_start"));
      model.set("appointment_end", getPara("model.appointment_end"));
      model.set("appointment_remark", getPara("model.appointment_remark"));
      model.set("appointment_save_time", timestamp);
      model.update();
    } 
    addOpLog("[????????????] ??????");
    renderSuccess();
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
  public void clearAppointment() {
    String[] idlist = getPara("idlist").split(",");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    for (int i = 0; i < idlist.length; i++) {
      String id = idlist[i];
      Rack model = (Rack)Rack.me.findById(id);
      Record record = new Record();
      record.setColumns(model);
      record.set("update_user", getSessionUser().getStr("display_name")).set("come_flag", "6")
        .set("update_time", timestamp).remove("id");
      Db.save("rack_log", record);
    } 
    String sql = "UPDATE rack SET appointment_id = NULL, appointment_name = null, appointment_start=null,appointment_end = NULL, appointment_remark = null,appointment_save_time=null WHERE id IN (" + 
      getPara("idlist") + ")";
    Db.update(sql);
    addOpLog("[??????] ??????????????????");
    renderSuccess();
  }
  
  public void exportCsv() {
    String select = "select a.* ";
    String sqlExceptSelect = " from rack a,sys_address_dict b where a.location  = b.id ";
    String position = getPara("position");
    String location = getPara("location");
    String project = getPara("project");
    if (StringUtils.isNotEmpty(position))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.position = '" + position + "'"; 
    if (StringUtils.isNotEmpty(project))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.project like '%" + project + "%'"; 
    if (StringUtils.isNotEmpty(location))
      sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and b.auto_val like '%" + location + "%'"; 
    sqlExceptSelect = String.valueOf(sqlExceptSelect) + "order by a.id desc";
    List<Record> list = Db.find(String.valueOf(select) + sqlExceptSelect);
    Map<String, Object> dictDatalocation = Rack.me.getDictDatalocation();
    Map<String, Object> dictDataposition = Rack.me.getDictDataposition();
    for (Record record : list) {
      String fieldName = "location";
      if (record.get(fieldName) != null && dictDatalocation.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatalocation.get(record.get(fieldName).toString())); 
      fieldName = "position";
      if (record.get(fieldName) != null && dictDataposition.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDataposition.get(record.get(fieldName).toString())); 
    } 
    List<String> headers = new ArrayList<String>();
    List<String> clomuns = new ArrayList<String>();
    headers.add("??????");
    clomuns.add("location");
    headers.add("?????????");
    clomuns.add("position");
    headers.add("????????????");
    clomuns.add("type");
    headers.add("?????????");
    clomuns.add("sn");
    headers.add("MAC??????");
    clomuns.add("mac_address");
    headers.add("?????????");
    clomuns.add("port");
    headers.add("IP??????");
    clomuns.add("ip_address");
    headers.add("????????????");
    clomuns.add("sub_mark");
    headers.add("??????");
    clomuns.add("gate_address");
    headers.add("?????????");
    clomuns.add("user");
    headers.add("???????????????");
    clomuns.add("pl_code");
    headers.add("????????????");
    clomuns.add("from_date");
    headers.add("????????????");
    clomuns.add("till_date");
    headers.add("??????");
    clomuns.add("project");
    headers.add("?????????");
    clomuns.add("sub_project");
    headers.add(" ????????????");
    clomuns.add("cc");
    headers.add("????????????");
    clomuns.add("tpm");
    headers.add("????????????");
    clomuns.add("pl");
    headers.add("??????");
    clomuns.add("remark");
    CsvRender csvRender = new CsvRender(headers, list);
    csvRender.clomuns(clomuns);
    csvRender.fileName("????????????");
    addOpLog("[????????????] ??????cvs");
    render(csvRender);
  }
  
  @Before({com.jfinal.plugin.activerecord.tx.Tx.class})
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
          ruleMap.put(Integer.valueOf(i), (String[])((List)ruleExcelListList.get(i)).toArray(new String[0])); 
        fis1 = new FileInputStream(uploadFile);
        bis1 = new BufferedInputStream(fis1);
        List<List<String>> readExcel2List = ExcelUtils.getInstance().readExcel2List(bis1, 1);
        if (readExcel2List == null || readExcel2List.isEmpty()) {
          result.put("result", "fail");
          result.put("msg", "??????????????????????????????????????????");
        } else {
          List<Record> find = Db.find("select id as k ,auto_val as v from sys_address_dict  where `status`='1'");
          List<Record> find3 = Db.find(getDictSql("?????????"));
          if (find == null || find.isEmpty()) {
            result.put("result", "fail");
            result.put("msg", "????????????????????????????????????????????????????????????");
          } else if (find3 == null || find3.isEmpty()) {
            result.put("result", "fail");
            result.put("msg", "???????????????????????????????????????????????????");
          } else {
            Map<String, Long> rackAddressMap = new HashMap<String, Long>();
            for (int i = 0; i < find.size(); i++) {
              Record record = (Record)find.get(i);
              rackAddressMap.put(record.getStr("v"), record.getLong("k"));
            } 
            Map<String, String> positionMap = CommonUtils.recordListToMap(find3);
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
                            listResult.add("Excel" + (i + 2) + "???" + strings[0] + 
                                "???????????????" + Integer.parseInt(split[2])); 
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
                        if (rackAddressMap.containsKey(v)) {
                          v = rackAddressMap.get(v);
                          break;
                        } 
                        valiflag = false;
                        listResult.add("Excel" + (i + 2) + "???" + strings[0] + "????????????????????????????????????");
                        break;
                      case 1:
                        if (positionMap.containsKey(v)) {
                          v = positionMap.get(v);
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
                Db.save("rack", (Record)batchRecord.get(i)); 
              result.put("result", "success");
              result.put("msg", "????????????,?????????" + batchRecord.size() + "?????????");
              logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "??????????????????????????????????????????" + batchRecord.size() + 
                  "?????????");
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
    Map<String, Object> dictDatalocation = Rack.me.getDictDatalocation();
    Map<String, Object> dictDataposition = Rack.me.getDictDataposition();
    for (Record record : paginate2) {
      String fieldName = "location";
      if (record.get(fieldName) != null && dictDatalocation.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDatalocation.get(record.get(fieldName).toString())); 
      fieldName = "position";
      if (record.get(fieldName) != null && dictDataposition.get(record.get(fieldName).toString()) != null)
        record.set(fieldName, dictDataposition.get(record.get(fieldName).toString())); 
    } 
    String fileName = "???????????????" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
    String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
    System.out.println(path);
    File file = new File(path);
    this.wb = new HSSFWorkbook();
    HSSFSheet sheet = this.wb.createSheet("???????????????");
    HSSFRow row0 = sheet.createRow(0);
    row0.createCell(0).setCellValue("??????");
    row0.createCell(1).setCellValue("?????????");
    row0.createCell(2).setCellValue("??????");
    row0.createCell(3).setCellValue("?????????");
    row0.createCell(4).setCellValue("MAC??????");
    row0.createCell(5).setCellValue("?????????");
    row0.createCell(6).setCellValue("IP??????");
    row0.createCell(7).setCellValue("????????????");
    row0.createCell(8).setCellValue("??????");
    row0.createCell(9).setCellValue("??????");
    row0.createCell(10).setCellValue("????????????");
    row0.createCell(11).setCellValue("NT??????");
    row0.createCell(12).setCellValue("LT??????");
    row0.createCell(13).setCellValue("OLT IP");
    row0.createCell(14).setCellValue("Port server??????");
    row0.createCell(15).setCellValue("Port serve??????");
    row0.createCell(16).setCellValue("STC/VSTC??????");
    row0.createCell(17).setCellValue("STC???NT??????");
    row0.createCell(18).setCellValue("LT???ONT??????");
    row0.createCell(19).setCellValue("ONT???Switch??????");
    row0.createCell(20).setCellValue("Switch???STC??????");
    row0.createCell(21).setCellValue("PC(PCTA)??????");
    row0.createCell(22).setCellValue("?????????");
    row0.createCell(23).setCellValue("?????????");
    row0.createCell(24).setCellValue("?????????");
    row0.createCell(25).setCellValue("????????????");
    row0.createCell(26).setCellValue("????????????");
    row0.createCell(27).setCellValue("??????");
    row0.createCell(28).setCellValue("?????????");
    row0.createCell(29).setCellValue("????????????");
    row0.createCell(30).setCellValue("????????????");
    row0.createCell(31).setCellValue("????????????");
    row0.createCell(32).setCellValue("??????");
    row0.createCell(33).setCellValue("???????????????");
    row0.createCell(34).setCellValue("???????????????");
    row0.createCell(35).setCellValue("???????????????");
    row0.createCell(36).setCellValue("????????????");
    for (int i = 0; i < paginate2.size(); i++) {
      Record record2 = (Record)paginate2.get(i);
      HSSFRow rowi = sheet.createRow(i + 1);
      rowi.createCell(0).setCellValue(isNull(record2.get("location")));
      rowi.createCell(1).setCellValue(isNull(record2.get("position")));
      rowi.createCell(2).setCellValue(isNull(record2.get("type")));
      rowi.createCell(3).setCellValue(isNull(record2.get("sn")));
      rowi.createCell(4).setCellValue(isNull(record2.get("mac_address")));
      rowi.createCell(5).setCellValue(isNull(record2.get("port")));
      rowi.createCell(6).setCellValue(isNull(record2.get("ip_address")));
      rowi.createCell(7).setCellValue(isNull(record2.get("sub_mark")));
      rowi.createCell(8).setCellValue(isNull(record2.get("gate_address")));
      rowi.createCell(9).setCellValue(isNull(record2.get("Status")));
      rowi.createCell(10).setCellValue(isNull(record2.get("platform_name")));
      rowi.createCell(11).setCellValue(isNull(record2.get("nt_info")));
      rowi.createCell(12).setCellValue(isNull(record2.get("lt_info")));
      rowi.createCell(13).setCellValue(isNull(record2.get("olt_ip")));
      rowi.createCell(14).setCellValue(isNull(record2.get("port_server_info")));
      rowi.createCell(15).setCellValue(isNull(record2.get("port_serve_link")));
      rowi.createCell(16).setCellValue(isNull(record2.get("stc_vstc_info")));
      rowi.createCell(17).setCellValue(isNull(record2.get("stc_nt_link")));
      rowi.createCell(18).setCellValue(isNull(record2.get("lt_ont_link")));
      rowi.createCell(19).setCellValue(isNull(record2.get("ont_switch_link")));
      rowi.createCell(20).setCellValue(isNull(record2.get("switch_stc_link")));
      rowi.createCell(21).setCellValue(isNull(record2.get("pcta_info")));
      rowi.createCell(22).setCellValue(isNull(record2.get("user")));
      rowi.createCell(23).setCellValue(isNull(record2.get("contact")));
      rowi.createCell(24).setCellValue(isNull(record2.get("pl_code")));
      rowi.createCell(25).setCellValue(isNull(record2.get("from_date")));
      rowi.createCell(26).setCellValue(isNull(record2.get("till_date")));
      rowi.createCell(27).setCellValue(isNull(record2.get("project")));
      rowi.createCell(28).setCellValue(isNull(record2.get("sub_project")));
      rowi.createCell(29).setCellValue(isNull(record2.get("cc")));
      rowi.createCell(30).setCellValue(isNull(record2.get("tpm")));
      rowi.createCell(31).setCellValue(isNull(record2.get("pl")));
      rowi.createCell(32).setCellValue(isNull(record2.get("remark")));
      rowi.createCell(33).setCellValue(isNull(record2.get("appointment_name")));
      rowi.createCell(34).setCellValue(appointmentTime(record2.get("appointment_start")));
      rowi.createCell(35).setCellValue(appointmentTime(record2.get("appointment_end")));
      rowi.createCell(36).setCellValue(isNull(record2.get("appointment_remark")));
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
