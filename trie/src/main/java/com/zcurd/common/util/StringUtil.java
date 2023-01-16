package com.zcurd.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.List;

public class StringUtil {
	public static <T> String join(Object[] objs, String splitString) {
		return join(objs, 0, objs.length, splitString);
	}

	public static <T> String join(Object[] objs, int start, int end, String splitString) {
		StringBuilder s = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (i != start)
				s.append(splitString);
			s.append(objs[i]);
		}
		return s.toString();
	}

	public static String join(List<?> objList, String splitString) {
		return join(objList, 0, objList.size(), splitString);
	}

	public static String join(List<?> objList, int start, int end, String splitString) {
		StringBuilder s = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (i != start)
				s.append(splitString);
			s.append(objList.get(i));
		}
		return s.toString();
	}

	public static <T> String join(List<T[]> objList, int columnIndex, String splitString) {
		StringBuilder s = new StringBuilder();
		if (objList.size() > 0) {
			s.append((Object[]) objList.get(0)[columnIndex]);
			for (int i = 1, ii = objList.size(); i < ii; i++)
				s.append(splitString).append((Object[]) objList.get(i)[columnIndex]);
		}
		return s.toString();
	}

	public static String repeat(String str, int repeat) {
		if (str == null)
			throw new NullPointerException("重复的字符串不能为null。");
		if (repeat < 0)
			throw new IllegalArgumentException("重复的次数(" + repeat + ")小于底限0。");
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < repeat; i++)
			s.append(str);
		return s.toString();
	}

	public static boolean equals(String string, String another) {
		if (string != null)
			return string.equals(another);
		if (another != null)
			return another.equals(string);
		return true;
	}

	public static boolean equalsIgnoreCase(String string, String another) {
		if (string != null)
			return string.equalsIgnoreCase(another);
		if (another != null)
			return another.equalsIgnoreCase(string);
		return true;
	}

	public static boolean contains(String strToFind, String... strings) {
		String[] var5 = strings;
		int var4 = strings.length;

		for (int i = 0; i < var4; ++i) {
			String s = var5[i];
			if (equals(s, strToFind)) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsIgnoreCase(String strToFind, String... strings) {
		String[] var5 = strings;
		int var4 = strings.length;

		for (int var3 = 0; var3 < var4; ++var3) {
			String s = var5[var3];
			if (equalsIgnoreCase(s, strToFind)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isEmpty(String s) {
		return !(s != null && s.trim().length() != 0);
	}

	public static boolean isEmptyAny(String... ss) {
		String[] var4 = ss;
		int var3 = ss.length;

		for (int var2 = 0; var2 < var3; ++var2) {
			String s = var4[var2];
			if (isEmpty(s)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isEmptyAll(String... ss) {
		String[] var4 = ss;
		int var3 = ss.length;

		for (int var2 = 0; var2 < var3; ++var2) {
			String s = var4[var2];
			if (isNotEmpty(s)) {
				return false;
			}
		}

		return true;
	}

	public static boolean isNotEmpty(String s) {
		return (s != null && s.trim().length() > 0);
	}

	public static boolean isNotEmptyAny(String... ss) {
		String[] var4 = ss;
		int var3 = ss.length;

		for (int var2 = 0; var2 < var3; ++var2) {
			String s = var4[var2];
			if (isNotEmpty(s)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isNotEmptyAll(String... ss) {
		String[] var4 = ss;
		int var3 = ss.length;

		for (int var2 = 0; var2 < var3; ++var2) {
			String s = var4[var2];
			if (isEmpty(s)) {
				return false;
			}
		}

		return true;
	}

	public static void checkEmpty(String s) {
		if (isEmpty(s))
			throw new IllegalStateException();
	}

	public static void checkEmpty(String s, String msg) {
		if (isEmpty(s))
			throw new IllegalStateException(msg);
	}

	public static void checkNull(Object o) {
		if (o == null)
			throw new NullPointerException();
	}

	public static void checkNull(Object o, String msg) {
		if (o == null)
			throw new NullPointerException(msg);
	}

	public static byte[] getBytes(String s, String charset) {
		try {
			return s.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static byte[] getBytes(String s, Charset charset) {
		return s.getBytes(charset);
	}

	public static String getString(byte[] bytes, String charset) {
		try {
			return new String(bytes, charset);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String getString(byte[] bytes, Charset charset) {
		return new String(bytes, charset);
	}

	public static byte[] hexStringToByteArray(String hexString) {
		if (hexString.length() % 2 != 0)
			throw new IllegalArgumentException("16进制数据长度不为2的倍数：" + hexString);
		StringReader stringReader = new StringReader(hexString);
		byte[] bytes = new byte[hexString.length() / 2];
		char[] chars = new char[2];
		try {
			for (int i = 0; stringReader.read(chars) != -1; i++)
				bytes[i] = (byte) Integer.parseInt(String.valueOf(chars), 16);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return bytes;
	}

	public static String stringToHexString(String string, String charset) {
		byte[] bytes = getBytes(string, charset);
		return byteArrayToHexString(bytes);
	}

	public static String stringToHexString(String string, Charset charset) {
		byte[] bytes = getBytes(string, charset);
		return byteArrayToHexString(bytes);
	}

	public static String byteArrayToHexString(byte[] bytes) {
		Formatter fmt = new Formatter(new StringBuilder(bytes.length * 2));
		byte[] var5 = bytes;
		int var4 = bytes.length;

		for (int var3 = 0; var3 < var4; ++var3) {
			byte b = var5[var3];
			fmt.format("%02x", b);
		}

		return fmt.toString();
	}

	public static boolean isDigit(String s) {
		return s.matches("^\\d+$");
	}

	public static boolean isAlpha(String s) {
		return s.matches("^[a-zA-Z]+$");
	}

	public static boolean isUpper(String s) {
		return s.matches("^[A-Z]+$");
	}

	public static boolean isLower(String s) {
		return s.matches("^[a-z]+$");
	}

	public static boolean isAlnum(String s) {
		return s.matches("^[a-zA-Z\\d]+$");
	}

	public static boolean isInt(String s) {
		return s.matches("^[+-]?\\d+$");
	}

	public static boolean isFloat(String s) {
		return s.matches("^[+-]?(0\\.\\d+|0|[1-9]\\d*(\\.\\d+)?)$");
	}

	public static boolean isEmail(String s) {
		return s.matches("^[a-zA-Z0-9._-]+@([a-zA-Z0-9_-])+(\\.[a-zA-Z0-9_-]+)+$");
	}

	public static boolean isIP(String s) {
		return s.matches("^(0?0?[1-9]|0?[1-9]\\d|1\\d\\d|2[01]\\d|22[0-3])(\\.([01]?\\d?\\d|2[0-4]\\d|25[0-5])){3}$");
	}

	public static String byteArrayToHexZeroString(byte[] bytes) {
		Formatter fmt = new Formatter(new StringBuilder(bytes.length * 2));
		byte[] var5 = bytes;
		int var4 = bytes.length;

		for (int var3 = 0; var3 < var4; ++var3) {
			byte b = var5[var3];
			if (b != 0) {
				fmt.format("%02x", b);
			}
		}

		return fmt.toString();
	}

	public static String capitalize(String str) {
		return changeFirstCharacterCase(str, true);
	}

	public static String uncapitalize(String str) {
		return changeFirstCharacterCase(str, false);
	}

	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0)
			return str;
		StringBuilder sb = new StringBuilder(str.length());
		if (capitalize) {
			sb.append(Character.toUpperCase(str.charAt(0)));
		} else {
			sb.append(Character.toLowerCase(str.charAt(0)));
		}
		sb.append(str.substring(1));
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(isEmail("luzhen@163.com"));
	}

	public static String readTxt2String(File file) {
		StringBuilder result = new StringBuilder();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(file));
			String s = null;

			while ((s = br.readLine()) != null) {
				result.append(System.lineSeparator() + s);
			}
		} catch (Exception var12) {
			var12.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException var11) {
					var11.printStackTrace();
				}
			}

		}

		return result.toString();
	}

	public static void saveToFile(String filePath, String content) {
		PrintStream ps = null;

		try {
			File file = new File(filePath);
			ps = new PrintStream(new FileOutputStream(file));
			ps.println(content);
		} catch (FileNotFoundException var7) {
			var7.printStackTrace();
		} finally {
			if (ps != null) {
				ps.flush();
				ps.close();
			}

		}
	}
}
