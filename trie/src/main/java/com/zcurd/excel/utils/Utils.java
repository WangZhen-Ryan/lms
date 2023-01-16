package com.zcurd.excel.utils;

import com.zcurd.excel.annotation.ExcelField;
import com.zcurd.excel.handler.ExcelHeader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;

public class Utils {
	public static List<ExcelHeader> getHeaderList(Class clz) {
		List<ExcelHeader> headers = new ArrayList<ExcelHeader>();
		List<Field> fields = new ArrayList<Field>();
		for (Class clazz = clz; clazz != Object.class; clazz = clazz.getSuperclass())
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		for (Field field : fields) {
			if (field.isAnnotationPresent(ExcelField.class)) {
				ExcelField er = (ExcelField) field.getAnnotation(ExcelField.class);
				headers.add(new ExcelHeader(er.title(), er.order(), field.getName(), field.getType()));
			}
		}
		Collections.sort(headers);
		return headers;
	}

	public static Map<Integer, ExcelHeader> getHeaderMap(Row titleRow, Class clz) {
		List<ExcelHeader> headers = getHeaderList(clz);
		Map<Integer, ExcelHeader> maps = new HashMap<Integer, ExcelHeader>();
		for (Cell c : titleRow) {
			String title = c.getStringCellValue();
			for (ExcelHeader eh : headers) {
				if (eh.getTitle().equals(title.trim())) {
					maps.put(Integer.valueOf(c.getColumnIndex()), eh);
					break;
				}
			}
		}
		return maps;
	}

	public static void outPutFile(Workbook wb, String outFilePath) {
		FileOutputStream fos = null;
		try {
			File f = new File(outFilePath);
			if (f.getParentFile().isDirectory() && !f.getParentFile().exists())
				f.mkdirs();
			if (!f.exists())
				f.createNewFile();
			fos = new FileOutputStream(outFilePath);
			wb.write(fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getCellValue(Workbook wb, Cell c) {
		return getCellValue(wb, c, null);
	}

	public static String getCellValue(Workbook wb, Cell c, String formatter) {
		FormulaEvaluator evaluator;
		String o = null;
		if (c == null)
			return o;
		switch (c.getCellType()) {
		case 3:
			return "";
		case 4:
			return String.valueOf(c.getBooleanCellValue());
		case 2:
			evaluator = wb.getCreationHelper().createFormulaEvaluator();
			return evaluator.evaluate(c).formatAsString();
		case 0:
			if (DateUtil.isCellDateFormatted(c)) {
				Date date = c.getDateCellValue();
				if (StringUtils.isNoneEmpty(new CharSequence[] { formatter })) {
					o = DateFormatUtils.format(date, formatter);
				} else {
					o = DateFormatUtils.format(date, "yyyy/MM/dd");
				}
			} else {
				o = NumberToTextConverter.toText(c.getNumericCellValue());
			}
			return o;
		case 1:
			return c.getStringCellValue();
		}
		return null;
	}

	public static Object str2TargetClass(String strField, Class clazz) throws Exception {
		if (strField == null || "".equals(strField))
			return null;
		if (Long.class == clazz || long.class == clazz) {
			strField = matchDoneBigDecimal(strField);
			strField = RegularUtils.converNumByReg(strField);
			return Long.valueOf(Long.parseLong(strField));
		}
		if (Integer.class == clazz || int.class == clazz) {
			strField = matchDoneBigDecimal(strField);
			strField = RegularUtils.converNumByReg(strField);
			return Integer.valueOf(Integer.parseInt(strField));
		}
		if (Float.class == clazz || float.class == clazz) {
			strField = matchDoneBigDecimal(strField);
			return Float.valueOf(Float.parseFloat(strField));
		}
		if (Double.class == clazz || double.class == clazz) {
			strField = matchDoneBigDecimal(strField);
			return Double.valueOf(Double.parseDouble(strField));
		}
		if (Character.class == clazz || char.class == clazz)
			return Character.valueOf(strField.toCharArray()[0]);
		if (Date.class == clazz)
			return DateUtils.str2DateUnmatch2Null(strField);
		return strField;
	}

	private static String matchDoneBigDecimal(String bigDecimal) {
		boolean flg = Pattern.matches("^-?\\d+(\\.\\d+)?(E-?\\d+)?$", bigDecimal);
		if (flg) {
			BigDecimal bd = new BigDecimal(bigDecimal);
			bigDecimal = bd.toPlainString();
		}
		return bigDecimal;
	}
}
