package com.zcurd.common.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Check {
	public static int BeginIndex(String temp) {
		try {
			Integer.parseInt(temp);
		} catch (Exception e) {
			return 0;
		}
		return Integer.parseInt(temp);
	}

	public static int EndIndex(String temp) {
		try {
			Integer.parseInt(temp);
		} catch (Exception e) {
			return 10;
		}
		return Integer.parseInt(temp);
	}

	public static String HadStr(String temp) throws UnsupportedEncodingException {
		if (temp == null || temp.equals("undefined") || temp.equals("null") || temp.equals("")) {
			temp = "";
		} else {
			temp = URLDecoder.decode(temp, "UTF-8");
		}
		return temp;
	}

	public static boolean IsStringNULL(String temp) {
		if (temp == null || temp.isEmpty() || "".equals(temp) || temp.equals("undefined") || temp.equals("null"))
			return true;
		return false;
	}

	public static Date IsDatetime(String temp) {
		Date curDate;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			curDate = formatter.parse(temp);
		} catch (Exception e) {
			e.printStackTrace();
			curDate = new Date();
		}
		formatter = null;
		return curDate;
	}

	public static String IsDatetime2(String temp) throws UnsupportedEncodingException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			formatter.parse(temp);
		} catch (Exception e) {
			e.printStackTrace();
			return formatter.format(new Date());
		}
		formatter = null;
		return temp;
	}

	public static boolean IsDatetime3(String temp) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			formatter.parse(temp);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		formatter = null;
		return true;
	}

	public static DateFormat getDateTimeFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public static DateFormat getDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}

	public static int IsNum(String temp) {
		try {
			Integer.parseInt(temp);
		} catch (Exception e) {
			return -1;
		}
		return Integer.parseInt(temp);
	}

	public static boolean isNumeric1(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}
		return true;
	}

	public static boolean isNumeric2(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	public static boolean isNumeric3(String str) {
		for (int i = str.length(); --i >= 0;) {
			int chr = str.charAt(i);
			if (chr < 48 || chr > 57)
				return false;
		}
		return true;
	}

	public static double round(double value, int scale, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, roundingMode);
		double d = bd.doubleValue();
		bd = null;
		return d;
	}

	public static String toUTF8(String str) throws UnsupportedEncodingException {
		if (str == null || str.isEmpty() || str.equals(""))
			return str;
		String retVal = str;
		try {
			retVal = new String(str.getBytes("ISO8859_1"), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}

	public static String trim(String s) throws UnsupportedEncodingException {
		int i = s.length();
		int j = 0;
		int k = 0;
		char[] arrayOfChar = s.toCharArray();
		while (j < i && arrayOfChar[k + j] <= ' ')
			j++;
		while (j < i && arrayOfChar[k + i - 1] <= ' ')
			i--;
		return (j > 0 || i < s.length()) ? s.substring(j, i) : s;
	}

	public static String trimAllToUpper(String s) throws UnsupportedEncodingException {
		if (s == null)
			return s;
		s = s.toUpperCase();
		return s.replaceAll("\\s*", "");
	}

	public static boolean IsValidPersonId(String strPersonId) {
		Pattern pat = Pattern.compile("^(\\d{17})(X|x|\\d)$|^\\d{18}$");
		Matcher matcher = pat.matcher(strPersonId);
		return matcher.matches();
	}

	public static boolean IsValidPersonName(String strPersonName) {
		Pattern pat = Pattern.compile("^(\\w|[一-龥]|\\·)*$");
		Matcher matcher = pat.matcher(strPersonName);
		return matcher.matches();
	}

	public static boolean isset(int a, int bit) {
		a >>= bit;
		return (a & 1) != 0;
	}

	public static boolean isMan(String strID) {
		if (IsStringNULL(strID) || (strID.length() != 15 && strID.length() != 18))
			return true;
		String lastValue = null;
		if (strID.length() == 15)
			lastValue = (new StringBuilder(String.valueOf(strID.substring(strID.length() - 1, strID.length()))))
					.toString();
		if (strID.length() == 18)
			lastValue = (new StringBuilder(String.valueOf(strID.substring(strID.length() - 2, strID.length() - 1))))
					.toString();
		int sex = IsNum(lastValue) % 2;
		return !(sex == 0);
	}

	public static int getAverage(List<Integer> arrSimilarity) {
		int iTotalSim = 0;
		int iTotalCount = 0;
		int iSimilarity = -100;
		for (Iterator iterator = arrSimilarity.iterator(); iterator.hasNext();) {
			int i = ((Integer) iterator.next()).intValue();
			if (i > 0) {
				iTotalSim += i;
				iTotalCount++;
				continue;
			}
			iSimilarity = i;
		}
		if (iTotalCount > 0)
			iSimilarity = iTotalSim / iTotalCount;
		return iSimilarity;
	}

	public static int getMax(List<Integer> arrSimilarity) {
		int iMaxSim = 0;
		for (Iterator iterator = arrSimilarity.iterator(); iterator.hasNext();) {
			int i = ((Integer) iterator.next()).intValue();
			if (i >= 0 && i <= 1000 && i < iMaxSim)
				iMaxSim = i;
		}
		return iMaxSim;
	}

	public static int getMaxNum(List<Integer> arrSimilarity) {
		int iMaxNum = 0;
		int iMaxSim = 0;
		for (int i = 0; i < arrSimilarity.size(); i++) {
			if (((Integer) arrSimilarity.get(i)).intValue() > iMaxSim) {
				iMaxNum = i;
				iMaxSim = ((Integer) arrSimilarity.get(i)).intValue();
			}
		}
		return iMaxNum;
	}

	public static void main(String[] args) {
	}
}
