package com.zcurd.ext.render.csv;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvUtil {
	private static UserSettings userSettings = new UserSettings();

	public static String createCSV(List headers, List data, List columns) {
		StringBuffer strOut = new StringBuffer("");
		if (headers != null && !headers.isEmpty())
			listToCSV(strOut, headers);
		if (data == null || data.isEmpty())
			return strOut.toString();
		Iterator itr = data.iterator();
		while (itr.hasNext()) {
			Object obj = itr.next();
			Class cls = obj.getClass();
			if (cls != null && cls.isArray()) {
				if (obj != null) {
					Object[] objs = (Object[]) obj;
					if (objs != null) {
						for (short i = 0; i < objs.length; i = (short) (i + 1)) {
							createCol(strOut, objs[i]);
							strOut.append(",");
						}
						strOut = strOut.deleteCharAt(strOut.length() - 1);
						strOut.append("\n");
					}
				}
			} else if (obj instanceof List) {
				List objlist = (List) obj;
				if (columns == null || columns.isEmpty()) {
					listToCSV(strOut, objlist);
				} else {
					for (int i = 0; i < columns.size(); i++) {
						createCol(strOut, objlist.get(((Integer) columns.get(i)).intValue()));
						strOut.append(",");
					}
					strOut = strOut.deleteCharAt(strOut.length() - 1);
					strOut.append("\n");
				}
			} else if (obj instanceof Map) {
				Map objmap = (Map) obj;
				if (columns == null || columns.isEmpty()) {
					Set keyset = objmap.keySet();
					for (Object key : keyset) {
						createCol(strOut, objmap.get(key));
						strOut.append(",");
					}
					strOut = strOut.deleteCharAt(strOut.length() - 1);
					strOut.append("\n");
				} else {
					for (int i = 0; i < columns.size(); i++) {
						createCol(strOut, objmap.get(columns.get(i)));
						strOut.append(",");
					}
					strOut = strOut.deleteCharAt(strOut.length() - 1);
					strOut.append("\n");
				}
			} else if (obj instanceof Model) {
		        Model objmodel = (Model)obj;
		        if (columns == null || columns.isEmpty()) {
		          Set<Map.Entry<String, Object>> entries = objmodel._getAttrsEntrySet();
		          for (Map.Entry<String, Object> entry : entries) {
		            createCol(strOut, entry.getValue());
		            strOut.append(",");
		          } 
		          strOut = strOut.deleteCharAt(strOut.length() - 1);
		          strOut.append("\n");
		        } else {
		          for (int i = 0; i < columns.size(); i++) {
		            createCol(strOut, objmodel.get((String)columns.get(i)));
		            strOut.append(",");
		          } 
		          strOut = strOut.deleteCharAt(strOut.length() - 1);
		          strOut.append("\n");
		        } 
			} else if (obj instanceof Record) {
				Record objrecord = (Record) obj;
				Map<String, Object> map = objrecord.getColumns();
				if (columns == null || columns.isEmpty()) {
					Set<String> keys = map.keySet();
					for (String key : keys) {
						createCol(strOut, objrecord.get(key));
						strOut.append(",");
					}
					strOut = strOut.deleteCharAt(strOut.length() - 1);
					strOut.append("\n");
				} else {
					for (int i = 0; i < columns.size(); i++) {
						strOut.append(",");
					}
					strOut = strOut.deleteCharAt(strOut.length() - 1);
					strOut.append("\n");
				}
			} else {
				while (itr.hasNext()) {
					Object objs = itr.next();
					if (objs != null) {
						createCol(strOut, objs);
						strOut.append("\n");
					}
				}
			}
			obj = null;
		}
		return strOut.toString();
	}

	public static void listToCSV(StringBuffer strOut, List<?> list) {
		if (list != null && !list.isEmpty()) {
			for (short i = 0; i < list.size(); i = (short) (i + 1)) {
				createCol(strOut, list.get(i));
				strOut.append(",");
			}
			strOut = strOut.deleteCharAt(strOut.length() - 1);
			strOut.append("\n");
		}
	}

	public static void createCol(StringBuffer strOut, Object obj) {
		if (obj != null) {
			strOut.append("\"");
			String content = null;
			if (obj instanceof Boolean) {
				content = ((Boolean) obj).toString();
			} else if (obj instanceof Calendar) {
				content = ((Calendar) obj).toString();
			} else if (obj instanceof Timestamp) {
				content = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(new Date(((Timestamp) obj).getTime()));
			} else if (obj instanceof Date) {
				content = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format((Date) obj);
			} else {
				content = write(String.valueOf(obj));
			}
			strOut.append(content);
			strOut.append("\"");
		} else {
			strOut.append("\" \" ");
		}
	}

	public static String write(String content) {
		boolean textQualify = userSettings.forceQualifier;
		if (content.length() > 0)
			content = content.trim();
		if (!textQualify && userSettings.useTextQualifier && (content.indexOf(userSettings.textQualifier) > -1
				|| content.indexOf(userSettings.delimiter) > -1 || content.indexOf('\n') > -1
				|| content.indexOf('\r') > -1 || content.indexOf(userSettings.recordDelimiter) > -1
				|| (content.length() > 0 && content.charAt(0) == userSettings.comment) || content.length() == 0))
			textQualify = true;
		if (userSettings.useTextQualifier && !textQualify && content.length() > 0) {
			char firstLetter = content.charAt(0);
			if (firstLetter == ' ' || firstLetter == '\t')
				textQualify = true;
			if (!textQualify && content.length() > 1) {
				char lastLetter = content.charAt(content.length() - 1);
				if (lastLetter == ' ' || lastLetter == '\t')
					textQualify = true;
			}
		}
		if (textQualify) {
			if (userSettings.escapeMode == 2) {
				content = replace(content, "\\", "\\\\");
				content = replace(content, String.valueOf(userSettings.textQualifier), "\\" + userSettings.textQualifier);
			} else {
				content = replace(content, String.valueOf(userSettings.textQualifier),
						String.valueOf(userSettings.textQualifier) + String.valueOf(userSettings.textQualifier));
			}
		} else if (userSettings.escapeMode == 2) {
			content = replace(content, "\\", "\\\\");
			content = replace(content, String.valueOf(userSettings.delimiter), "\\" + userSettings.delimiter);
			content = replace(content, "\r", "\\\r");
			content = replace(content, "\n", "\\\n");
		}
		return content;
	}

	public static String replace(String original, String pattern, String replace) {
		int len = pattern.length();
		int found = original.indexOf(pattern);
		if (found > -1) {
			StringBuffer sb = new StringBuffer();
			int start = 0;
			while (found != -1) {
				sb.append(original.substring(start, found));
				sb.append(replace);
				start = found + len;
				found = original.indexOf(pattern, start);
			}
			sb.append(original.substring(start));
			return sb.toString();
		}
		return original;
	}
}
