package com.zcurd.excel;

import com.zcurd.excel.handler.ExcelHeader;
import com.zcurd.excel.handler.ExcelTemplate;
import com.zcurd.excel.utils.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
	private static ExcelUtils excelUtils = new ExcelUtils();

	public static ExcelUtils getInstance() {
		return excelUtils;
	}

	public <T> List<T> readExcel2Objects(String excelPath, Class<T> clazz, int offsetLine, int limitLine,
			int sheetIndex) throws Exception {
		Workbook workbook = WorkbookFactory.create(new File(excelPath));
		return readExcel2ObjectsHandler(workbook, clazz, offsetLine, limitLine, sheetIndex);
	}

	public <T> List<T> readExcel2Objects(InputStream is, Class<T> clazz, int offsetLine, int limitLine, int sheetIndex)
			throws Exception {
		Workbook workbook = WorkbookFactory.create(is);
		return readExcel2ObjectsHandler(workbook, clazz, offsetLine, limitLine, sheetIndex);
	}

	public <T> List<T> readExcel2Objects(String excelPath, Class<T> clazz, int sheetIndex) throws Exception {
		return readExcel2Objects(excelPath, clazz, 0, 2147483647, sheetIndex);
	}

	public <T> List<T> readExcel2Objects(String excelPath, Class<T> clazz) throws Exception {
		return readExcel2Objects(excelPath, clazz, 0, 2147483647, 0);
	}

	public <T> List<T> readExcel2Objects(InputStream is, Class<T> clazz, int sheetIndex) throws Exception {
		return readExcel2Objects(is, clazz, 0, 2147483647, sheetIndex);
	}

	public <T> List<T> readExcel2Objects(InputStream is, Class<T> clazz) throws Exception {
		return readExcel2Objects(is, clazz, 0, 2147483647, 0);
	}

	private <T> List<T> readExcel2ObjectsHandler(Workbook workbook, Class<T> clazz, int offsetLine, int limitLine,
			int sheetIndex) throws Exception {
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		Row row = sheet.getRow(offsetLine);
		List<T> list = new ArrayList<T>();
		Map<Integer, ExcelHeader> maps = Utils.getHeaderMap(row, clazz);
		if (maps == null || maps.size() <= 0)
			throw new RuntimeException("要读取的Excel的格式不正确，检查是否设定了合适的行");
		int maxLine = (sheet.getLastRowNum() > offsetLine + limitLine) ? (offsetLine + limitLine)
				: sheet.getLastRowNum();
		for (int i = offsetLine + 1; i <= maxLine; i++) {
			row = sheet.getRow(i);
			T obj = (T) clazz.newInstance();
			for (Cell cell : row) {
				int ci = cell.getColumnIndex();
				ExcelHeader header = (ExcelHeader) maps.get(Integer.valueOf(ci));
				if (header == null)
					continue;
				String filed = header.getFiled();
				String val = Utils.getCellValue(workbook, cell);
				Object value = Utils.str2TargetClass(val, header.getFiledClazz());
				BeanUtils.copyProperty(obj, filed, value);
			}
			list.add(obj);
		}
		return list;
	}

	public List<List<String>> readExcel2List(String excelPath, int offsetLine, int limitLine, int sheetIndex)
			throws Exception {
		Workbook workbook = WorkbookFactory.create(new File(excelPath));
		return readExcel2ObjectsHandler(workbook, offsetLine, limitLine, sheetIndex);
	}

	public List<List<String>> readExcel2List(InputStream is, int offsetLine, int limitLine, int sheetIndex)
			throws Exception {
		Workbook workbook = WorkbookFactory.create(is);
		return readExcel2ObjectsHandler(workbook, offsetLine, limitLine, sheetIndex);
	}

	public List<List<String>> readExcel2List(String excelPath, int offsetLine) throws Exception {
		Workbook workbook = WorkbookFactory.create(new File(excelPath));
		return readExcel2ObjectsHandler(workbook, offsetLine, 2147483647, 0);
	}

	public List<List<String>> readExcel2List(InputStream is, int offsetLine) throws Exception {
		Workbook workbook = WorkbookFactory.create(is);
		return readExcel2ObjectsHandler(workbook, offsetLine, 2147483647, 0);
	}

	public List<List<String>> readExcel2List(String excelPath) throws Exception {
		Workbook workbook = WorkbookFactory.create(new File(excelPath));
		return readExcel2ObjectsHandler(workbook, 0, 2147483647, 0);
	}

	public List<List<String>> readExcel2List(InputStream is) throws Exception {
		Workbook workbook = WorkbookFactory.create(is);
		return readExcel2ObjectsHandler(workbook, 0, 2147483647, 0);
	}

	private List<List<String>> readExcel2ObjectsHandler(Workbook workbook, int offsetLine, int limitLine,
			int sheetIndex) throws Exception {
		List<List<String>> list = new ArrayList<List<String>>();
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		int maxLine = 0;
		int mmax = (limitLine == Integer.MAX_VALUE || offsetLine == Integer.MAX_VALUE) ? Integer.MAX_VALUE
				: (offsetLine + limitLine);
		if (sheet.getLastRowNum() > mmax) {
			maxLine = offsetLine + limitLine;
		} else {
			maxLine = sheet.getLastRowNum();
		}
		for (int i = offsetLine; i <= maxLine; i++) {
			List<String> rows = new ArrayList<String>();
			Row row = sheet.getRow(i);
			short j = 0;
			short lastCellNum = row.getLastCellNum();
			while (j < lastCellNum) {
				String val = Utils.getCellValue(workbook, row.getCell(j));
				rows.add(val);
				j = (short) (j + 1);
			}
			list.add(rows);
		}
		return list;
	}

	public List<List<String>> readExcel2List(String excelPath, int offsetLine, int limitLine, String sheetName)
			throws Exception {
		Workbook workbook = WorkbookFactory.create(new File(excelPath));
		return readExcel2ObjectsHandler(workbook, offsetLine, limitLine, sheetName);
	}

	public List<List<String>> readExcel2List(InputStream is, int offsetLine, String sheetName) throws Exception {
		Workbook workbook = WorkbookFactory.create(is);
		return readExcel2ObjectsHandler(workbook, offsetLine, 2147483647, sheetName);
	}

	private List<List<String>> readExcel2ObjectsHandler(Workbook workbook, int offsetLine, int limitLine,
			String sheetName) throws Exception {
		List<List<String>> list = new ArrayList<List<String>>();
		Sheet sheet = workbook.getSheet(sheetName);
		int maxLine = 0;
		int mmax = (limitLine == Integer.MAX_VALUE || offsetLine == Integer.MAX_VALUE) ? Integer.MAX_VALUE
				: (offsetLine + limitLine);
		if (sheet.getLastRowNum() > mmax) {
			maxLine = offsetLine + limitLine;
		} else {
			maxLine = sheet.getLastRowNum();
		}
		for (int i = offsetLine; i <= maxLine; i++) {
			List<String> rows = new ArrayList<String>();
			Row row = sheet.getRow(i);
			short j = 0;
			short lastCellNum = row.getLastCellNum();
			while (j < lastCellNum) {
				String val = Utils.getCellValue(workbook, row.getCell(j));
				rows.add(val);
				j = (short) (j + 1);
			}
			list.add(rows);
		}
		return list;
	}

	public void exportObjects2Excel(String templatePath, int sheetIndex, List<?> data, Map<String, String> extendMap,
			Class clazz, boolean isWriteHeader, String targetPath) throws Exception {
		exportExcelByModuleHandler(templatePath, sheetIndex, data, extendMap, clazz, isWriteHeader)
				.write2File(targetPath);
	}

	public void exportObjects2Excel(String templatePath, int sheetIndex, List<?> data, Map<String, String> extendMap,
			Class clazz, boolean isWriteHeader, OutputStream os) throws Exception {
		exportExcelByModuleHandler(templatePath, sheetIndex, data, extendMap, clazz, isWriteHeader).write2Stream(os);
	}

	public void exportObjects2Excel(String templatePath, List<?> data, Map<String, String> extendMap, Class clazz,
			boolean isWriteHeader, String targetPath) throws Exception {
		exportObjects2Excel(templatePath, 0, data, extendMap, clazz, isWriteHeader, targetPath);
	}

	public void exportObjects2Excel(String templatePath, List<?> data, Map<String, String> extendMap, Class clazz,
			boolean isWriteHeader, OutputStream os) throws Exception {
		exportObjects2Excel(templatePath, 0, data, extendMap, clazz, isWriteHeader, os);
	}

	public void exportObjects2Excel(String templatePath, List<?> data, Map<String, String> extendMap, Class clazz,
			String targetPath) throws Exception {
		exportObjects2Excel(templatePath, 0, data, extendMap, clazz, false, targetPath);
	}

	public void exportObjects2Excel(String templatePath, List<?> data, Map<String, String> extendMap, Class clazz,
			OutputStream os) throws Exception {
		exportObjects2Excel(templatePath, 0, data, extendMap, clazz, false, os);
	}

	public void exportObjects2Excel(String templatePath, List<?> data, Class clazz, String targetPath)
			throws Exception {
		exportObjects2Excel(templatePath, 0, data, null, clazz, false, targetPath);
	}

	public void exportObjects2Excel(String templatePath, List<?> data, Class clazz, OutputStream os) throws Exception {
		exportObjects2Excel(templatePath, 0, data, null, clazz, false, os);
	}

	private ExcelTemplate exportExcelByModuleHandler(String templatePath, int sheetIndex, List<?> data,
			Map<String, String> extendMap, Class clazz, boolean isWriteHeader) throws Exception {
		ExcelTemplate templates = ExcelTemplate.getInstance(templatePath, sheetIndex);
		templates.extendData(extendMap);
		List<ExcelHeader> headers = Utils.getHeaderList(clazz);
		if (isWriteHeader) {
			templates.createNewRow();
			for (ExcelHeader header : headers)
				templates.createCell(header.getTitle(), null);
		}
		for (Object object : data) {
			templates.createNewRow();
			templates.insertSerial(null);
			for (ExcelHeader header : headers)
				templates.createCell(BeanUtils.getProperty(object, header.getFiled()), null);
		}
		return templates;
	}

	public void exportObject2Excel(String templatePath, int sheetIndex, Map<String, List> data,
			Map<String, String> extendMap, Class clazz, boolean isWriteHeader, String targetPath) throws Exception {
		exportExcelByModuleHandler(templatePath, sheetIndex, data, extendMap, clazz, isWriteHeader)
				.write2File(targetPath);
	}

	public void exportObject2Excel(String templatePath, int sheetIndex, Map<String, List> data,
			Map<String, String> extendMap, Class clazz, boolean isWriteHeader, OutputStream os) throws Exception {
		exportExcelByModuleHandler(templatePath, sheetIndex, data, extendMap, clazz, isWriteHeader).write2Stream(os);
	}

	public void exportObject2Excel(String templatePath, Map<String, List> data, Map<String, String> extendMap,
			Class clazz, String targetPath) throws Exception {
		exportExcelByModuleHandler(templatePath, 0, data, extendMap, clazz, false).write2File(targetPath);
	}

	public void exportObject2Excel(String templatePath, Map<String, List> data, Map<String, String> extendMap,
			Class clazz, OutputStream os) throws Exception {
		exportExcelByModuleHandler(templatePath, 0, data, extendMap, clazz, false).write2Stream(os);
	}

	private ExcelTemplate exportExcelByModuleHandler(String templatePath, int sheetIndex, Map<String, List> data,
			Map<String, String> extendMap, Class clazz, boolean isWriteHeader) throws Exception {
		ExcelTemplate templates = ExcelTemplate.getInstance(templatePath, sheetIndex);
		templates.extendData(extendMap);
		List<ExcelHeader> headers = Utils.getHeaderList(clazz);
		if (isWriteHeader) {
			templates.createNewRow();
			for (ExcelHeader header : headers)
				templates.createCell(header.getTitle(), null);
		}
		for (Map.Entry<String, List> entry : data.entrySet()) {
			for (Object object : (List) entry.getValue()) {
				templates.createNewRow();
				templates.insertSerial((String) entry.getKey());
				for (ExcelHeader header : headers)
					templates.createCell(BeanUtils.getProperty(object, header.getFiled()), (String) entry.getKey());
			}
		}
		return templates;
	}

	public void exportObjects2Excel(List<?> data, Class clazz, boolean isWriteHeader, String sheetName, boolean isXSSF,
			String targetPath) throws Exception {
		FileOutputStream fos = new FileOutputStream(targetPath);
		exportExcelNoModuleHandler(data, clazz, isWriteHeader, sheetName, isXSSF).write(fos);
	}

	public void exportObjects2Excel(List<?> data, Class clazz, boolean isWriteHeader, String sheetName, boolean isXSSF,
			OutputStream os) throws Exception {
		exportExcelNoModuleHandler(data, clazz, isWriteHeader, sheetName, isXSSF).write(os);
	}

	public void exportObjects2Excel(List<?> data, Class clazz, boolean isWriteHeader, String targetPath)
			throws Exception {
		FileOutputStream fos = new FileOutputStream(targetPath);
		exportExcelNoModuleHandler(data, clazz, isWriteHeader, null, true).write(fos);
	}

	public void exportObjects2Excel(List<?> data, Class clazz, boolean isWriteHeader, OutputStream os)
			throws Exception {
		exportExcelNoModuleHandler(data, clazz, isWriteHeader, null, true).write(os);
	}

	private Workbook exportExcelNoModuleHandler(List<?> data, Class clazz, boolean isWriteHeader, String sheetName,
			boolean isXSSF) throws Exception {
		Sheet sheet;
		Workbook workbook;
		if (isXSSF) {
			XSSFWorkbook xSSFWorkbook = new XSSFWorkbook();
			if (sheetName != null && !"".equals(sheetName)) {
				sheet = xSSFWorkbook.createSheet(sheetName);
			} else {
				sheet = xSSFWorkbook.createSheet();
			}
			workbook = xSSFWorkbook;
		} else {
			HSSFWorkbook hSSFWorkbook = new HSSFWorkbook();
			if (sheetName != null && !"".equals(sheetName)) {
				sheet = hSSFWorkbook.createSheet(sheetName);
			} else {
				sheet = hSSFWorkbook.createSheet();
			}
			workbook = hSSFWorkbook;
		}

		Row row = sheet.createRow(0);
		List<ExcelHeader> headers = Utils.getHeaderList(clazz);
		if (isWriteHeader)
			for (int i = 0; i < headers.size(); i++)
				row.createCell(i).setCellValue(((ExcelHeader) headers.get(i)).getTitle());
		for (int i = 0; i < data.size(); i++) {
			row = sheet.createRow(i + 1);
			Object _data = data.get(i);
			for (int j = 0; j < headers.size(); j++)
				row.createCell(j).setCellValue(BeanUtils.getProperty(_data, ((ExcelHeader) headers.get(j)).getFiled()));
		}
		return workbook;
	}

	public void exportObjects2Excel(List<?> data, List<String> header, String sheetName, boolean isXSSF,
			String targetPath) throws Exception {
		exportExcelNoModuleHandler(data, header, sheetName, isXSSF).write(new FileOutputStream(targetPath));
	}

	public void exportObjects2Excel(List<?> data, List<String> header, String sheetName, boolean isXSSF,
			OutputStream os) throws Exception {
		exportExcelNoModuleHandler(data, header, sheetName, isXSSF).write(os);
	}

	public void exportObjects2Excel(List<?> data, List<String> header, String targetPath) throws Exception {
		exportExcelNoModuleHandler(data, header, null, true).write(new FileOutputStream(targetPath));
	}

	public void exportObjects2Excel(List<?> data, List<String> header, OutputStream os) throws Exception {
		exportExcelNoModuleHandler(data, header, null, true).write(os);
	}

	public void exportObjects2Excel(List<?> data, String targetPath) throws Exception {
		exportExcelNoModuleHandler(data, null, null, true).write(new FileOutputStream(targetPath));
	}

	public void exportObjects2Excel(List<?> data, OutputStream os) throws Exception {
		exportExcelNoModuleHandler(data, null, null, true).write(os);
	}

	private Workbook exportExcelNoModuleHandler(List<?> data, List<String> header, String sheetName, boolean isXSSF)
			throws Exception {
		Sheet sheet;
		Workbook workbook;
		if (isXSSF) {
			XSSFWorkbook xSSFWorkbook = new XSSFWorkbook();
			if (sheetName != null && !"".equals(sheetName)) {
				sheet = xSSFWorkbook.createSheet(sheetName);
			} else {
				sheet = xSSFWorkbook.createSheet();
			}
			workbook = xSSFWorkbook;
		} else {
			HSSFWorkbook hSSFWorkbook = new HSSFWorkbook();
			if (sheetName != null && !"".equals(sheetName)) {
				sheet = hSSFWorkbook.createSheet(sheetName);
			} else {
				sheet = hSSFWorkbook.createSheet();
			}
			workbook = hSSFWorkbook;
		}

		int rowIndex = 0;
		if (header != null && header.size() > 0) {
			Row row = sheet.createRow(rowIndex);
			for (int i = 0; i < header.size(); i++)
				row.createCell(i, 1).setCellValue((String) header.get(i));
			rowIndex++;
		}
		for (Object object : data) {
			Row row = sheet.createRow(rowIndex);
			if (object.getClass().isArray()) {
				for (int j = 0; j < Array.getLength(object); j++)
					row.createCell(j, 1).setCellValue(Array.get(object, j).toString());
			} else if (object instanceof Collection) {
				Collection<?> items = (Collection) object;
				int j = 0;
				for (Object item : items) {
					row.createCell(j, 1).setCellValue(item.toString());
					j++;
				}
			} else {
				row.createCell(0, 1).setCellValue(object.toString());
			}
			rowIndex++;
		}
		return workbook;
	}
}
