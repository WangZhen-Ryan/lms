package com.zcurd.excel.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelTemplate {
  private Workbook workbook;
  
  private Sheet sheet;
  
  private int sheetIndex;
  
  private Row currentRow;
  
  private CellStyle currentStyle;
  
  private int currentColumnIndex;
  
  private int currentRowIndex;
  
  private CellStyle defaultStyle;
  
  private Map<Integer, CellStyle> appointLineStyle = new HashMap();
  
  private Map<String, CellStyle> classifyStyle = new HashMap();
  
  private CellStyle singleLineStyle;
  
  private CellStyle doubleLineStyle;
  
  private int initColumnIndex;
  
  private int initRowIndex;
  
  private int lastRowIndex;
  
  private float rowHeight;
  
  private int serialNumberColumnIndex = -1;
  
  private int serialNumber;
  
  public static ExcelTemplate getInstance(String templatePath, int sheetIndex) {
    ExcelTemplate template = new ExcelTemplate();
    template.sheetIndex = sheetIndex;
    try {
      template.loadTemplate(templatePath);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return template;
  }
  
  private ExcelTemplate loadTemplate(String templatePath) throws Exception {
    this.workbook = WorkbookFactory.create(new File(templatePath));
    this.sheet = this.workbook.getSheetAt(this.sheetIndex);
    initModuleConfig();
    this.currentRowIndex = this.initRowIndex;
    this.currentColumnIndex = this.initColumnIndex;
    this.lastRowIndex = this.sheet.getLastRowNum();
    return this;
  }
  
  private void initModuleConfig() {
    for (Row row : this.sheet) {
      for (Cell c : row) {
        if (c.getCellType() != 1)
          continue; 
        String str = c.getStringCellValue().trim();
        if (str.equals("$serial_number"))
          this.serialNumberColumnIndex = c.getColumnIndex(); 
        if (str.equals("$data_index")) {
          this.initColumnIndex = c.getColumnIndex();
          this.initRowIndex = row.getRowNum();
          this.rowHeight = row.getHeightInPoints();
        } 
        initStyles(c, str);
      } 
    } 
  }
  
  private void initStyles(Cell cell, String moduleContext) {
    if ("$default_style".equals(moduleContext)) {
      this.defaultStyle = cell.getCellStyle();
      clearCell(cell);
    } 
    if (moduleContext != null && moduleContext.startsWith("&")) {
      this.classifyStyle.put(moduleContext.substring(1), cell.getCellStyle());
      clearCell(cell);
    } 
    if ("$appoint_line_style".equals(moduleContext)) {
      this.appointLineStyle.put(Integer.valueOf(cell.getRowIndex()), cell.getCellStyle());
      clearCell(cell);
    } 
    if ("$single_line_style".equals(moduleContext)) {
      this.singleLineStyle = cell.getCellStyle();
      clearCell(cell);
    } 
    if ("$double_line_style".equals(moduleContext)) {
      this.doubleLineStyle = cell.getCellStyle();
      clearCell(cell);
    } 
  }
  
  private void clearCell(Cell cell) {
    cell.setCellStyle(null);
    cell.setCellValue("");
  }
  
  public void extendData(Map<String, String> data) {
    if (data == null)
      return; 
    for (Row row : this.sheet) {
      for (Cell c : row) {
        if (c.getCellType() != 1)
          continue; 
        String str = c.getStringCellValue().trim();
        if (str.startsWith("#") && 
          data.containsKey(str.substring(1)))
          c.setCellValue((String)data.get(str.substring(1))); 
      } 
    } 
  }
  
  public void createNewRow() {
    if (this.lastRowIndex > this.currentRowIndex && this.currentRowIndex != this.initRowIndex) {
      this.sheet.shiftRows(this.currentRowIndex, this.lastRowIndex, 1, true, true);
      this.lastRowIndex++;
    } 
    this.currentRow = this.sheet.createRow(this.currentRowIndex);
    this.currentRow.setHeightInPoints(this.rowHeight);
    this.currentRowIndex++;
    this.currentColumnIndex = this.initColumnIndex;
  }
  
  public void insertSerial(String styleKey) {
    if (this.serialNumberColumnIndex < 0)
      return; 
    this.serialNumber++;
    Cell c = this.currentRow.createCell(this.serialNumberColumnIndex);
    setCellStyle(c, styleKey);
    c.setCellValue(this.serialNumber);
  }
  
  public void createCell(Object value, String styleKey) {
    Cell cell = this.currentRow.createCell(this.currentColumnIndex);
    setCellStyle(cell, styleKey);
    if (value == null || "".equals(value)) {
      this.currentColumnIndex++;
      return;
    } 
    if (String.class == value.getClass()) {
      cell.setCellValue((String)value);
      this.currentColumnIndex++;
      return;
    } 
    if (int.class == value.getClass()) {
      cell.setCellValue(((Integer)value).intValue());
      this.currentColumnIndex++;
      return;
    } 
    if (Integer.class == value.getClass()) {
      cell.setCellValue(((Integer)value).intValue());
      this.currentColumnIndex++;
      return;
    } 
    if (double.class == value.getClass()) {
      cell.setCellValue(((Double)value).doubleValue());
      this.currentColumnIndex++;
      return;
    } 
    if (Double.class == value.getClass()) {
      cell.setCellValue(((Double)value).doubleValue());
      this.currentColumnIndex++;
      return;
    } 
    if (Date.class == value.getClass()) {
      cell.setCellValue((Date)value);
      this.currentColumnIndex++;
      return;
    } 
    if (boolean.class == value.getClass()) {
      cell.setCellValue(((Boolean)value).booleanValue());
      this.currentColumnIndex++;
      return;
    } 
    if (Boolean.class == value.getClass()) {
      cell.setCellValue(((Boolean)value).booleanValue());
      this.currentColumnIndex++;
      return;
    } 
    if (Calendar.class == value.getClass()) {
      cell.setCellValue((Calendar)value);
      this.currentColumnIndex++;
      return;
    } 
    this.currentColumnIndex++;
  }
  
  private void setCellStyle(Cell cell, String styleKey) {
    if (styleKey != null && this.classifyStyle.get(styleKey) != null) {
      cell.setCellStyle((CellStyle)this.classifyStyle.get(styleKey));
      return;
    } 
    if (this.appointLineStyle != null && this.appointLineStyle.containsKey(Integer.valueOf(cell.getRowIndex()))) {
      cell.setCellStyle((CellStyle)this.appointLineStyle.get(Integer.valueOf(cell.getRowIndex())));
      return;
    } 
    if (this.singleLineStyle != null && cell.getRowIndex() % 2 != 0) {
      cell.setCellStyle(this.singleLineStyle);
      return;
    } 
    if (this.doubleLineStyle != null && cell.getRowIndex() % 2 == 0) {
      cell.setCellStyle(this.doubleLineStyle);
      return;
    } 
    if (this.defaultStyle != null)
      cell.setCellStyle(this.defaultStyle); 
  }
  
  public void write2File(String filepath) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(filepath);
      this.workbook.write(fos);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("写入的文件不存在");
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("写入数据失败:" + e.getMessage());
    } finally {
      try {
        if (fos != null)
          fos.close(); 
      } catch (IOException e) {
        e.printStackTrace();
      } 
    } 
  }
  
  public void write2Stream(OutputStream os) {
    try {
      this.workbook.write(os);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("写入流失败:" + e.getMessage());
    } 
  }
}
