package com.zcurd.common;

import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.util.StringUtil;
import com.zcurd.vo.ZcurdMeta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZcurdTool {
	public static Map<String, String> getQueryPara(Map<String, String[]> paraMap) {
		Map<String, String> queryPara = new HashMap<String, String>();
		for (String paraName : paraMap.keySet())
			queryPara.put(paraName, ((String[]) paraMap.get(paraName))[0]);
		return queryPara;
	}

	public static Record replaceDict(ZcurdMeta metaData, Record record) {
		Map<String, Map<String, Object>> dictData = metaData.getDictMap();
		for (String key : dictData.keySet()) {
			if (record.get(key) != null) {
				String dictValueStr = "";
				byte b;
				String[] arrayOfString = record.get(key).toString().split(",");

				for (int i = 0; i < arrayOfString.length; i++) {
					String fieldValue = arrayOfString[i];
					Object dictValue = ((Map) dictData.get(key)).get(fieldValue);
					if (dictValue != null) {
						dictValueStr = String.valueOf(dictValueStr) + "," + dictValue.toString();
					} else {
						dictValueStr = String.valueOf(dictValueStr) + "," + fieldValue;
					}
				}
				record.set("_" + key, record.get(key));
				record.set(key, dictValueStr.substring(1));
			}
		}
		return record;
	}

	public static void replaceDict(Map<String, Object> dictData, Record record, String fieldName) {
		record.set("_" + fieldName, record.get(fieldName));
		if (record.get(fieldName) != null && dictData.get(record.get(fieldName).toString()) != null)
			record.set(fieldName, dictData.get(record.get(fieldName).toString()));
	}

	public static void replaceDict(Map<String, Object> dictData, List<Record> recordList, String fieldName) {
		for (Record record : recordList)
			replaceDict(dictData, record, fieldName);
	}

	public static List<Record> replaceDict(int headId, List<Record> list) {
		return replaceDict(DbMetaTool.getMetaData(headId), list);
	}

	public static List<Record> replaceDict(ZcurdMeta metaData, List<Record> list) {
		for (Record record : list)
			replaceDict(metaData, record);
		return list;
	}

	public static String getDbSource(String dbSource) {
		if (StringUtil.isEmpty(dbSource))
			dbSource = DbKit.getConfig().getName();
		return dbSource;
	}
}
